package org.saripalli.aigateway.provider.anthropic

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

// --- Request Models ---

data class AnthropicMessageRequest(
    val model: String,
    val messages: List<AnthropicMessage>,
    val system: String? = null,
    @JsonProperty("max_tokens")
    val maxTokens: Int = 4096,
    val temperature: Double? = null,
    val stream: Boolean? = null,
    val thinking: AnthropicThinkingConfig? = null
)

data class AnthropicMessage(
    val role: String,
    val content: String
)

data class AnthropicThinkingConfig(
    val type: String = "enabled",
    @JsonProperty("budget_tokens")
    val budgetTokens: Int
)

// --- Response Models ---

@JsonIgnoreProperties(ignoreUnknown = true)
data class AnthropicMessageResponse(
    val id: String = "",
    val type: String = "",
    val role: String = "",
    val model: String = "",
    val content: List<AnthropicContent> = emptyList(),
    @JsonProperty("stop_reason")
    val stopReason: String? = null,
    val usage: AnthropicUsage? = null,
    val error: AnthropicError? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AnthropicContent(
    val type: String = "text",
    val text: String? = null,
    val thinking: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AnthropicUsage(
    @JsonProperty("input_tokens")
    val inputTokens: Int = 0,
    @JsonProperty("output_tokens")
    val outputTokens: Int = 0,
    @JsonProperty("cache_creation_input_tokens")
    val cacheCreationInputTokens: Int = 0,
    @JsonProperty("cache_read_input_tokens")
    val cacheReadInputTokens: Int = 0
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AnthropicError(
    val type: String? = null,
    val message: String? = null
)