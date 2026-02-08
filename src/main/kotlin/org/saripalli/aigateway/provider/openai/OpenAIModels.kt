package org.saripalli.aigateway.provider.openai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Request body for OpenAI-compatible APIs.
 * Uses @JsonInclude to omit null fields so reasoning models don't receive unsupported params.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OpenAIRequestBody(
    val model: String,
    val messages: List<OpenAIMessage>,
    val temperature: Double? = null,
    @JsonProperty("max_tokens")
    val maxTokens: Int? = null,
    @JsonProperty("max_completion_tokens")
    val maxCompletionTokens: Int? = null,
    val stream: Boolean? = null
)

data class OpenAIMessage(
    val role: String,
    val content: String
)

// --- Response Models ---

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAIResponse(
    val id: String = "",
    @JsonProperty("object")
    val objectType: String = "",
    val created: Long = 0,
    val model: String = "",
    val choices: List<OpenAIChoice> = emptyList(),
    val usage: OpenAIUsage? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAIChoice(
    val index: Int = 0,
    val message: OpenAIMessage? = null,
    @JsonProperty("finish_reason")
    val finishReason: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAIUsage(
    @JsonProperty("prompt_tokens")
    val promptTokens: Int = 0,
    @JsonProperty("completion_tokens")
    val completionTokens: Int = 0,
    @JsonProperty("total_tokens")
    val totalTokens: Int = 0
)