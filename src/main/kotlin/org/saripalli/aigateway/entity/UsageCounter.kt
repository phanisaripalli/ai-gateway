package org.saripalli.aigateway.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Table("usage_counters")
data class UsageCounter(
    @Column("project_id")
    val projectId: UUID,
    val date: LocalDate,
    @Column("total_tokens")
    val totalTokens: Long = 0,
    @Column("total_cost_usd")
    val totalCostUsd: BigDecimal = BigDecimal.ZERO,
    @Column("request_count")
    val requestCount: Int = 0
)