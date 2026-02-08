package org.saripalli.aigateway.service

import org.saripalli.aigateway.dto.ChatCompletionRequest
import org.springframework.stereotype.Service
import java.math.BigDecimal

data class ModelConfig(
    val id: String,
    val provider: String,
    val capability: String?,
    val inputCostPer1M: BigDecimal,
    val outputCostPer1M: BigDecimal,
    val thinkingCostPer1M: BigDecimal? = null
)

@Service
class ModelRegistry {

    private val models: Map<String, ModelConfig> = buildModelMap()

    // capability -> provider -> model ID
    private val capabilityMap: Map<String, Map<String, String>> = mapOf(
        "fast" to mapOf(
            "gemini" to "gemini-2.0-flash",
            "openai" to "gpt-4.1-mini",
            "anthropic" to "claude-haiku-4-5",
            "groq" to "llama-3.3-70b-versatile"
        ),
        "balanced" to mapOf(
            "gemini" to "gemini-2.5-pro",
            "openai" to "gpt-4.1",
            "anthropic" to "claude-sonnet-4-5",
            "groq" to "llama-3.3-70b-versatile"
        ),
        "thinking" to mapOf(
            "gemini" to "gemini-2.5-flash-thinking",
            "openai" to "o3",
            "anthropic" to "claude-sonnet-4-5",
            "groq" to "qwen-qwq-32b"
        ),
        "best" to mapOf(
            "gemini" to "gemini-2.5-pro",
            "openai" to "gpt-4.1",
            "anthropic" to "claude-opus-4-5",
            "groq" to "llama-3.3-70b-versatile"
        )
    )

    /**
     * Resolves the concrete model and provider from a request.
     * If a capability is specified, looks up the default model for that capability.
     * Otherwise, looks up the model by ID.
     *
     * @param request The chat completion request
     * @param projectDefaultProvider The project's default provider (from project settings)
     */
    fun resolveModel(request: ChatCompletionRequest, projectDefaultProvider: String? = null): ModelConfig {
        val capability = request.capability?.lowercase()

        if (capability != null) {
            val providerMap = capabilityMap[capability]
                ?: throw IllegalArgumentException(
                    "Unknown capability: '$capability'. Available: ${capabilityMap.keys}"
                )

            // Priority: 1) request.provider, 2) project default, 3) system default order
            val provider = request.provider?.lowercase()
                ?: projectDefaultProvider?.lowercase()

            if (provider != null) {
                val modelId = providerMap[provider]
                    ?: throw IllegalArgumentException(
                        "Provider '$provider' has no model for capability '$capability'. Available providers: ${providerMap.keys}"
                    )
                return getModelConfig(modelId)
            }

            // No provider specified — pick first available (prefer gemini as default)
            val defaultOrder = listOf("gemini", "openai", "anthropic", "groq")
            for (p in defaultOrder) {
                val modelId = providerMap[p]
                if (modelId != null) {
                    return getModelConfig(modelId)
                }
            }
            throw IllegalStateException("No model found for capability '$capability'")
        }

        // Direct model ID
        return getModelConfig(request.model)
    }

    fun getModelConfig(modelId: String): ModelConfig {
        return models[modelId.lowercase()]
            ?: ModelConfig(
                id = modelId,
                provider = inferProvider(modelId),
                capability = null,
                inputCostPer1M = BigDecimal("3.00"),
                outputCostPer1M = BigDecimal("15.00")
            )
    }

    private fun inferProvider(model: String): String {
        val m = model.lowercase()
        return when {
            m.startsWith("gemini") -> "gemini"
            m.startsWith("gpt") || m.startsWith("o1") || m.startsWith("o3") || m.startsWith("o4") -> "openai"
            m.startsWith("claude") -> "anthropic"
            m.startsWith("llama") || m.startsWith("qwen") -> "groq"
            else -> "unknown"
        }
    }

    private fun buildModelMap(): Map<String, ModelConfig> {
        val configs = listOf(
            // Gemini
            ModelConfig("gemini-2.0-flash", "gemini", "fast", bd("0.10"), bd("0.40")),
            ModelConfig("gemini-2.5-pro", "gemini", "balanced", bd("1.25"), bd("10.00")),
            ModelConfig("gemini-2.5-flash-thinking", "gemini", "thinking", bd("0.10"), bd("0.40"), bd("0.10")),

            // OpenAI
            ModelConfig("gpt-4.1-mini", "openai", "fast", bd("0.40"), bd("1.60")),
            ModelConfig("gpt-4.1", "openai", "balanced", bd("2.00"), bd("8.00")),
            ModelConfig("gpt-4o", "openai", "balanced", bd("2.50"), bd("10.00")),
            ModelConfig("o3", "openai", "thinking", bd("2.00"), bd("8.00"), bd("8.00")),
            ModelConfig("o4-mini", "openai", "thinking", bd("1.10"), bd("4.40"), bd("4.40")),

            // Anthropic
            ModelConfig("claude-haiku-4-5", "anthropic", "fast", bd("0.80"), bd("4.00")),
            ModelConfig("claude-sonnet-4-5", "anthropic", "balanced", bd("3.00"), bd("15.00")),
            ModelConfig("claude-opus-4-5", "anthropic", "best", bd("15.00"), bd("75.00")),

            // Groq
            ModelConfig("llama-3.3-70b-versatile", "groq", "fast", bd("0.59"), bd("0.79")),
            ModelConfig("qwen-qwq-32b", "groq", "thinking", bd("0.20"), bd("0.20"))
        )
        return configs.associateBy { it.id.lowercase() }
    }

    private fun bd(value: String) = BigDecimal(value)
}