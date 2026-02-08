package org.saripalli.aigateway.controller

import org.saripalli.aigateway.dto.ChatCompletionRequest
import org.saripalli.aigateway.dto.ChatCompletionResponse
import org.saripalli.aigateway.security.GatewayContext
import org.saripalli.aigateway.security.GatewayContextHolder
import org.saripalli.aigateway.service.*
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class ChatController(
    private val providerRegistry: ProviderRegistry,
    private val tokenService: TokenService,
    private val modelRegistry: ModelRegistry,
    private val costService: CostService,
    private val usageTracker: UsageTracker,
    private val requestLoggingService: RequestLoggingService
) {

    private val log = LoggerFactory.getLogger(ChatController::class.java)

    @PostMapping("/v1/chat/completions", produces = ["application/json"])
    fun chatCompletions(@RequestBody request: ChatCompletionRequest): Mono<ChatCompletionResponse> {
        validate(request)
        val startTime = System.currentTimeMillis()

        return GatewayContextHolder.getContext()
            .flatMap { ctx ->
                val modelConfig = modelRegistry.resolveModel(request, ctx.defaultProvider)
                val resolvedRequest = request.copy(
                    model = modelConfig.id,
                    provider = modelConfig.provider
                )

                val estimatedInputTokens = tokenService.countTokens(resolvedRequest.messages)
                val estimatedCost = costService.estimateCost(estimatedInputTokens, modelConfig)

                log.info(
                    "Chat request: project={}, provider={}, model={}, capability={}, estimatedInputTokens={}, estimatedCost={}",
                    ctx.projectId, modelConfig.provider, modelConfig.id, request.capability,
                    estimatedInputTokens, estimatedCost
                )

                // Pre-flight limit check
                usageTracker.checkLimits(ctx.projectId, estimatedInputTokens, estimatedCost)
                    .then(callProvider(resolvedRequest, modelConfig.provider, ctx.projectId))
                    .map { response -> enrichResponse(response, modelConfig.provider) }
                    .flatMap { enriched ->
                        val latencyMs = System.currentTimeMillis() - startTime
                        // Log request + update usage counters (async, fire-and-forget)
                        requestLoggingService.logRequest(ctx, enriched, request.capability, latencyMs)
                            .then(Mono.just(enriched))
                    }
                    .onErrorResume { ex ->
                        val latencyMs = System.currentTimeMillis() - startTime
                        // Log failed requests too
                        requestLoggingService.logError(
                            ctx, modelConfig.provider, modelConfig.id,
                            request.capability, latencyMs,
                            ex.javaClass.simpleName, ex.message
                        ).then(Mono.error(ex))
                    }
            }
    }

    private fun callProvider(request: ChatCompletionRequest, providerName: String, projectId: java.util.UUID): Mono<ChatCompletionResponse> {
        val provider = providerRegistry.getProvider(providerName)
        return provider.chat(request, projectId)
    }

    private fun enrichResponse(response: ChatCompletionResponse, provider: String): ChatCompletionResponse {
        val cost = response.usage?.let { costService.calculateCost(it, response.model) }
        return response.copy(
            provider = provider,
            cost = cost
        )
    }

    private fun validate(request: ChatCompletionRequest) {
        if (request.capability.isNullOrBlank()) {
            require(request.model.isNotBlank()) { "Field 'model' or 'capability' is required" }
        }
        require(request.messages.isNotEmpty()) { "Field 'messages' must not be empty" }
    }
}