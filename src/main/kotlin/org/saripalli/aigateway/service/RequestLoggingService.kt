package org.saripalli.aigateway.service

import org.saripalli.aigateway.dto.ChatCompletionResponse
import org.saripalli.aigateway.entity.Request
import org.saripalli.aigateway.repository.ProviderUsageRepository
import org.saripalli.aigateway.repository.RequestRepository
import org.saripalli.aigateway.repository.UsageCounterRepository
import org.saripalli.aigateway.security.GatewayContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.math.BigDecimal
import java.time.LocalDate

@Service
class RequestLoggingService(
    private val requestRepository: RequestRepository,
    private val usageCounterRepository: UsageCounterRepository,
    private val providerUsageRepository: ProviderUsageRepository
) {

    private val log = LoggerFactory.getLogger(RequestLoggingService::class.java)

    /**
     * Logs the completed request and updates usage counters.
     * Runs asynchronously so it doesn't block the response.
     */
    fun logRequest(
        ctx: GatewayContext,
        response: ChatCompletionResponse,
        capability: String?,
        latencyMs: Long
    ): Mono<Void> {
        val usage = response.usage
        val cost = response.cost?.total ?: BigDecimal.ZERO
        val inputTokens = usage?.prompt_tokens ?: 0
        val outputTokens = usage?.completion_tokens ?: 0
        val totalTokens = (usage?.total_tokens ?: 0).toLong()

        val requestEntity = Request(
            projectId = ctx.projectId,
            apiKeyId = ctx.apiKeyId,
            provider = response.provider ?: "unknown",
            model = response.model,
            capability = capability,
            inputTokens = inputTokens,
            outputTokens = outputTokens,
            costUsd = cost,
            latencyMs = latencyMs.toInt(),
            status = "success"
        )

        // Save request log and increment usage counters in parallel
        val saveRequest = requestRepository.save(requestEntity).then()
        val updateProjectCounters = usageCounterRepository.incrementUsage(
            ctx.projectId, LocalDate.now(), totalTokens, cost
        )
        val updateProviderCounters = providerUsageRepository.incrementUsage(
            response.provider ?: "unknown", LocalDate.now(), totalTokens, cost
        )

        return Mono.`when`(saveRequest, updateProjectCounters, updateProviderCounters)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnError { e -> log.error("Failed to log request: {}", e.message) }
            .onErrorResume { Mono.empty() } // Fire-and-forget: don't fail the response
    }

    /**
     * Logs a failed request (error from upstream provider).
     */
    fun logError(
        ctx: GatewayContext,
        provider: String,
        model: String,
        capability: String?,
        latencyMs: Long,
        errorCode: String?,
        errorMessage: String?
    ): Mono<Void> {
        val requestEntity = Request(
            projectId = ctx.projectId,
            apiKeyId = ctx.apiKeyId,
            provider = provider,
            model = model,
            capability = capability,
            inputTokens = 0,
            outputTokens = 0,
            costUsd = BigDecimal.ZERO,
            latencyMs = latencyMs.toInt(),
            status = "error",
            errorCode = errorCode,
            errorMessage = errorMessage
        )

        return requestRepository.save(requestEntity).then()
            .subscribeOn(Schedulers.boundedElastic())
            .doOnError { e -> log.error("Failed to log error request: {}", e.message) }
            .onErrorResume { Mono.empty() }
    }
}