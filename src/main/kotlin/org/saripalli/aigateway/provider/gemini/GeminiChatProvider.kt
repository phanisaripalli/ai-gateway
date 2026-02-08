package org.saripalli.aigateway.provider.gemini

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
class GeminiChatProvider(
    private val geminiWebClient: WebClient,
    private val geminiConfig: GeminiConfig,
    private val credentialService: ProviderCredentialService
) : ChatProvider {

    private val log = LoggerFactory.getLogger(GeminiChatProvider::class.java)

    override fun getProviderName(): String = "gemini"

    override fun chat(request: ChatCompletionRequest, projectId: UUID): Mono<ChatCompletionResponse> {
        val geminiRequest = toGeminiRequest(request)
        val model = request.model

        // Get API key: project credential first, then fall back to global config
        return getApiKey(projectId)
            .flatMap { apiKey ->
                geminiWebClient.post()
                    .uri("/v1beta/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .bodyValue(geminiRequest)
                    .retrieve()
                    .bodyToMono(GeminiGenerateContentResponse::class.java)
                    .map { response -> toOpenAiResponse(response, model) }
                    .onErrorResume(WebClientResponseException::class.java) { ex ->
                        log.error("Gemini API error: {} {}", ex.statusCode, ex.responseBodyAsString)
                        Mono.error(mapGeminiError(ex))
                    }
            }
    }

    private fun getApiKey(projectId: UUID): Mono<String> {
        return credentialService.getDecryptedKey(projectId, "gemini")
            .switchIfEmpty(Mono.defer {
                try {
                    Mono.just(geminiConfig.getApiKey())
                } catch (e: Exception) {
                    Mono.error(e)
                }
            })
    }

    private fun toGeminiRequest(request: ChatCompletionRequest): GeminiGenerateContentRequest {
        val systemMessages = request.messages.filter { it.role == "system" }
        val conversationMessages = request.messages.filter { it.role != "system" }

        val systemInstruction = if (systemMessages.isNotEmpty()) {
            GeminiContent(
                role = "user",
                parts = systemMessages.map { GeminiPart(it.content) }
            )
        } else null

        val contents = conversationMessages.map { msg ->
            GeminiContent(
                role = mapRole(msg.role),
                parts = listOf(GeminiPart(msg.content))
            )
        }

        val generationConfig = GeminiGenerationConfig(
            temperature = request.temperature,
            maxOutputTokens = request.maxTokens
        )

        return GeminiGenerateContentRequest(
            contents = contents,
            generationConfig = generationConfig,
            systemInstruction = systemInstruction
        )
    }

    private fun toOpenAiResponse(
        response: GeminiGenerateContentResponse,
        model: String
    ): ChatCompletionResponse {
        val candidate = response.candidates?.firstOrNull()
        val text = candidate?.content?.parts?.firstOrNull()?.text ?: ""
        val metadata = response.usageMetadata

        return ChatCompletionResponse(
            id = "chatcmpl-${UUID.randomUUID()}",
            created = System.currentTimeMillis() / 1000,
            model = model,
            choices = listOf(
                ChatCompletionChoice(
                    index = 0,
                    message = ChatMessage(role = "assistant", content = text),
                    finishReason = mapFinishReason(candidate?.finishReason)
                )
            ),
            usage = metadata?.let {
                Usage(
                    prompt_tokens = it.promptTokenCount,
                    completion_tokens = it.candidatesTokenCount,
                    total_tokens = it.totalTokenCount
                )
            }
        )
    }

    private fun mapRole(openAiRole: String): String = when (openAiRole) {
        "assistant" -> "model"
        else -> "user"
    }

    private fun mapFinishReason(geminiReason: String?): String? = when (geminiReason) {
        "STOP" -> "stop"
        "MAX_TOKENS" -> "length"
        "SAFETY" -> "content_filter"
        else -> geminiReason?.lowercase()
    }

    private fun mapGeminiError(ex: WebClientResponseException): Exception {
        val status = ex.statusCode.value()
        val body = ex.responseBodyAsString
        return when (status) {
            400 -> IllegalArgumentException("Gemini bad request: $body")
            403 -> SecurityException("Gemini authentication failed: $body")
            429 -> RuntimeException("Gemini rate limit exceeded: $body")
            else -> RuntimeException("Gemini API error ($status): $body")
        }
    }
}