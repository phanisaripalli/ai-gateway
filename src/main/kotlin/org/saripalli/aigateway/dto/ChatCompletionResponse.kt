package org.saripalli.aigateway.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ChatCompletionChoice(
    val index: Int,
    val message: ChatMessage,
    @JsonProperty("finish_reason")
    val finishReason: String? = null
)

data class ChatCompletionResponse(
    val id: String,
    @JsonProperty("object")
    val objectType: String = "chat.completion",
    val created: Long,
    val model: String,
    val provider: String? = null,
    val choices: List<ChatCompletionChoice>,
    val usage: Usage? = null,
    val cost: CostBreakdown? = null
)

data class CostBreakdown(
    val input: java.math.BigDecimal,
    val output: java.math.BigDecimal,
    val total: java.math.BigDecimal,
    val currency: String = "USD"
)