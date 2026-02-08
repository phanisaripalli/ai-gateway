package org.saripalli.aigateway.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class CreateProjectRequest(
    val name: String,
    val description: String? = null,
    @JsonProperty("daily_token_limit")
    val dailyTokenLimit: Long? = null,
    @JsonProperty("daily_cost_limit")
    val dailyCostLimit: BigDecimal? = null,
    @JsonProperty("monthly_token_limit")
    val monthlyTokenLimit: Long? = null,
    @JsonProperty("monthly_cost_limit")
    val monthlyCostLimit: BigDecimal? = null,
    @JsonProperty("default_provider")
    val defaultProvider: String? = null,
    @JsonProperty("default_capability")
    val defaultCapability: String? = "balanced"
)

data class ProjectResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    @JsonProperty("daily_token_limit")
    val dailyTokenLimit: Long?,
    @JsonProperty("daily_cost_limit")
    val dailyCostLimit: BigDecimal?,
    @JsonProperty("monthly_token_limit")
    val monthlyTokenLimit: Long?,
    @JsonProperty("monthly_cost_limit")
    val monthlyCostLimit: BigDecimal?,
    @JsonProperty("default_provider")
    val defaultProvider: String?,
    @JsonProperty("default_capability")
    val defaultCapability: String?,
    @JsonProperty("is_active")
    val isActive: Boolean,
    @JsonProperty("created_at")
    val createdAt: LocalDateTime?,
    @JsonProperty("updated_at")
    val updatedAt: LocalDateTime?
)