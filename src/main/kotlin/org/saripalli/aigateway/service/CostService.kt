package org.saripalli.aigateway.service

import org.saripalli.aigateway.dto.CostBreakdown
import org.saripalli.aigateway.dto.Usage
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class CostService(
    private val modelRegistry: ModelRegistry
) {

    private val oneMillionTokens = BigDecimal("1000000")

    /**
     * Estimates the input cost before making the provider call (for preflight checks).
     */
    fun estimateCost(estimatedInputTokens: Int, modelConfig: ModelConfig): BigDecimal {
        return BigDecimal(estimatedInputTokens)
            .multiply(modelConfig.inputCostPer1M)
            .divide(oneMillionTokens, 6, RoundingMode.HALF_UP)
    }

    fun calculateCost(usage: Usage, modelId: String): CostBreakdown {
        val config = modelRegistry.getModelConfig(modelId)

        val inputCost = BigDecimal(usage.prompt_tokens)
            .multiply(config.inputCostPer1M)
            .divide(oneMillionTokens, 6, RoundingMode.HALF_UP)

        val outputCost = BigDecimal(usage.completion_tokens)
            .multiply(config.outputCostPer1M)
            .divide(oneMillionTokens, 6, RoundingMode.HALF_UP)

        val total = inputCost.add(outputCost)

        return CostBreakdown(
            input = inputCost,
            output = outputCost,
            total = total
        )
    }
}