package org.saripalli.aigateway.provider.anthropic

import org.saripalli.aigateway.dto.*
import org.saripalli.aigateway.service.ChatProvider
import org.saripalli.aigateway.service.ProviderCredentialService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class AnthropicChatProvider(
    private val anthropicWebClient: WebClient,
    private val anthropicConfig: AnthropicConfig,
    private val credentialService: ProviderCredentialService
) : ChatProvider {

    private val log = LoggerFactory.getLogger(AnthropicChatProvider::class.java)

    override fun getProviderName(): String = "anthropic"

    override fun chat(request: ChatCompletionRequest, projectId: UUID): Mono<ChatCompletionResponse> {
        val anthropicRequest = toAnthropicRequest(request)

        return getApiKey(projectId)
            .flatMap { apiKey ->
                anthropicWebClient.post()
                    .uri("/v1/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", anthropicConfig.getApiVersion())
                    .header("content-type", "application/json")
                    .bodyValue(anthropicRequest)
                    .retrieve()
                    .bodyToMono(AnthropicMessageResponse::class.java)
                    .map { response -> toOpenAiResponse(response, request.model) }
                    .onErrorResume(WebClientResponseException::class.java) { ex ->
                        log.error("Anthropic API error: {} {}", ex.statusCode, ex.responseBodyAsString)
                        Mono.error(mapAnthropicError(ex))
                    }
            }
    }

    private fun getApiKey(projectId: UUID): Mono<String> {
        return credentialService.getDecryptedKey(projectId, "anthropic")
            .switchIfEmpty(Mono.defer {
                try {
                    Mono.just(anthropicConfig.getApiKey())
                } catch (e: Exception) {
                    Mono.error(e)
                }
            })
    }

    private fun toAnthropicRequest(request: ChatCompletionRequest): AnthropicMessageRequest {
        val systemMessages = request.messages.filter { it.role == "system" }
        val conversationMessages = request.messages.filter { it.role != "system" }

        val systemPrompt = if (systemMessages.isNotEmpty()) {
            systemMessages.joinToString("\n") { it.content }
        } else null

        val messages = conversationMessages.map { msg ->
            AnthropicMessage(
                role = msg.role,
                content = msg.content
            )
        }

        val maxTokens = request.maxTokens ?: 4096

        return AnthropicMessageRequest(
            model = request.model,
            messages = messages,
            system = systemPrompt,
            maxTokens = maxTokens,
            temperature = request.temperature,
            stream = if (request.stream == true) true else null
        )
    }

    private fun toOpenAiResponse(
        response: AnthropicMessageResponse,
        model: String
    ): ChatCompletionResponse {
        val textContent = response.content
            .filter { it.type == "text" }
            .mapNotNull { it.text }
            .joinToString("")

        val usage = response.usage

        return ChatCompletionResponse(
            id = "chatcmpl-${UUID.randomUUID()}",
            created = System.currentTimeMillis() / 1000,
            model = model,
            choices = listOf(
                ChatCompletionChoice(
                    index = 0,
                    message = ChatMessage(role = "assistant", content = textContent),
                    finishReason = mapStopReason(response.stopReason)
                )
            ),
            usage = usage?.let {
                Usage(
                    prompt_tokens = it.inputTokens,
                    completion_tokens = it.outputTokens,
                    total_tokens = it.inputTokens + it.outputTokens
                )
            }
        )
    }

    private fun mapStopReason(stopReason: String?): String? = when (stopReason) {
        "end_turn" -> "stop"
        "max_tokens" -> "length"
        "stop_sequence" -> "stop"
        else -> stopReason
    }

    private fun mapAnthropicError(ex: WebClientResponseException): Exception {
        val status = ex.statusCode.value()
        val body = ex.responseBodyAsString
        return when (status) {
            400 -> IllegalArgumentException("Anthropic bad request: $body")
            401 -> SecurityException("Anthropic authentication failed: $body")
            429 -> RuntimeException("Anthropic rate limit exceeded: $body")
            529 -> RuntimeException("Anthropic API overloaded: $body")
            else -> RuntimeException("Anthropic API error ($status): $body")
        }
    }
}
