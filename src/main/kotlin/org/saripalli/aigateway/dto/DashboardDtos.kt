package org.saripalli.aigateway.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class OverviewStats(
    @JsonProperty("requests_today") val requestsToday: Long,
    @JsonProperty("requests_month") val requestsMonth: Long,
    @JsonProperty("cost_today") val costToday: BigDecimal,
    @JsonProperty("cost_month") val costMonth: BigDecimal,
    @JsonProperty("active_projects") val activeProjects: Long
)

data class DailyCost(
    val date: LocalDate,
    val cost: BigDecimal
)

data class ProviderStats(
    val provider: String,
    @JsonProperty("request_count") val requestCount: Long,
    @JsonProperty("total_cost") val totalCost: BigDecimal,
    @JsonProperty("total_tokens") val totalTokens: Long
)

data class ModelStats(
    val model: String,
    val provider: String,
    @JsonProperty("request_count") val requestCount: Long
)

data class ActivityEntry(
    val id: UUID,
    val provider: String,
    val model: String,
    val status: String,
    val cost: BigDecimal,
    val tokens: Int,
    @JsonProperty("latency_ms") val latencyMs: Int?,
    @JsonProperty("created_at") val createdAt: LocalDateTime?
)
