package org.saripalli.aigateway.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Table("projects")
data class Project(
    @Id
    val id: UUID? = null,
    val name: String,
    val description: String? = null,
    @Column("daily_token_limit")
    val dailyTokenLimit: Long? = null,
    @Column("daily_cost_limit")
    val dailyCostLimit: BigDecimal? = null,
    @Column("monthly_token_limit")
    val monthlyTokenLimit: Long? = null,
    @Column("monthly_cost_limit")
    val monthlyCostLimit: BigDecimal? = null,
    @Column("default_provider")
    val defaultProvider: String? = null,
    @Column("default_capability")
    val defaultCapability: String? = "balanced",
    @Column("is_active")
    val isActive: Boolean = true,
    @Column("created_at")
    val createdAt: LocalDateTime? = null,
    @Column("updated_at")
    val updatedAt: LocalDateTime? = null
)