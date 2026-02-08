package org.saripalli.aigateway.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ChatCompletionRequest(
    val model: String = "",
    val messages: List<ChatMessage>,
    val temperature: Double? = 0.7,
    @JsonProperty("max_tokens")
    val maxTokens: Int? = null,
    val stream: Boolean? = false,
    val provider: String? = null,
    val capability: String? = null
)
