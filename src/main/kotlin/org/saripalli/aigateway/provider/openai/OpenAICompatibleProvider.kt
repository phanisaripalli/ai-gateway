package org.saripalli.aigateway.provider.openai

import org.saripalli.aigateway.dto.*
import org.saripalli.aigateway.service.ChatProvider
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.util.UUID

/**
 * Base class for OpenAI-compatible providers (OpenAI, Groq, etc.).
 * Subclasses provide the WebClient, API key, and provider name.
 */
abstract class OpenAICompatibleProvider : ChatProvider {

    private val log = LoggerFactory.getLogger(this::class.java)

    protected abstract fun webClient(): WebClient
    protected abstract fun getApiKey(projectId: UUID): Mono<String>

    override fun chat(request: ChatCompletionRequest, projectId: UUID): Mono<ChatCompletionResponse> {
        val requestBody = toRequestBody(request)

        return getApiKey(projectId)
            .flatMap { apiKey ->
                webClient().post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer $apiKey")
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(OpenAIResponse::class.java)
                    .map { response -> toOpenAiResponse(response) }
                    .onErrorResume(WebClientResponseException::class.java) { ex ->
                        log.error("{} API error: {} {}", getProviderName(), ex.statusCode, ex.responseBodyAsString)
                        Mono.error(mapError(ex))
                    }
            }
    }

    private fun toRequestBody(request: ChatCompletionRequest): OpenAIRequestBody {
        val isReasoningModel = isReasoningModel(request.model)

        val messages = request.messages.map { msg ->
            OpenAIMessage(role = msg.role, content = msg.content)
        }

        return if (isReasoningModel) {
            // Reasoning models: no temperature, use max_completion_tokens instead of max_tokens
            OpenAIRequestBody(
                model = request.model,
                messages = messages,
                temperature = null,
                maxTokens = null,
                maxCompletionTokens = request.maxTokens,
                stream = if (request.stream == true) true else null
            )
        } else {
            OpenAIRequestBody(
                model = request.model,
                messages = messages,
                temperature = request.temperature,
                maxTokens = request.maxTokens,
                stream = if (request.stream == true) true else null
            )
        }
    }

    private fun toOpenAiResponse(response: OpenAIResponse): ChatCompletionResponse {
        val choice = response.choices.firstOrNull()

        return ChatCompletionResponse(
            id = response.id.ifBlank { "chatcmpl-${UUID.randomUUID()}" },
            created = if (response.created > 0) response.created else System.currentTimeMillis() / 1000,
            model = response.model,
            choices = listOf(
                ChatCompletionChoice(
                    index = 0,
                    message = ChatMessage(
                        role = choice?.message?.role ?: "assistant",
                        content = choice?.message?.content ?: ""
                    ),
                    finishReason = choice?.finishReason
                )
            ),
            usage = response.usage?.let {
                Usage(
                    prompt_tokens = it.promptTokens,
                    completion_tokens = it.completionTokens,
                    total_tokens = it.totalTokens
                )
            }
        )
    }

    private fun isReasoningModel(model: String): Boolean {
        val m = model.lowercase()
        return m.startsWith("o1") || m.startsWith("o3") || m.startsWith("o4")
    }

    private fun mapError(ex: WebClientResponseException): Exception {
        val status = ex.statusCode.value()
        val body = ex.responseBodyAsString
        val provider = getProviderName()
        return when (status) {
            400 -> IllegalArgumentException("$provider bad request: $body")
            401 -> SecurityException("$provider authentication failed: $body")
            429 -> RuntimeException("$provider rate limit exceeded: $body")
            else -> RuntimeException("$provider API error ($status): $body")
        }
    }
}