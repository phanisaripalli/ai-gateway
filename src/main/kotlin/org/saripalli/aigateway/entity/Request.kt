package org.saripalli.aigateway.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Table("requests")
data class Request(
    @Id
    val id: UUID? = null,
    @Column("project_id")
    val projectId: UUID,
    @Column("api_key_id")
    val apiKeyId: UUID?,
    val provider: String,
    val model: String,
    val capability: String? = null,
    @Column("input_tokens")
    val inputTokens: Int,
    @Column("output_tokens")
    val outputTokens: Int,
    @Column("thinking_tokens")
    val thinkingTokens: Int = 0,
    @Column("cost_usd")
    val costUsd: BigDecimal,
    @Column("latency_ms")
    val latencyMs: Int? = null,
    val status: String,
    @Column("error_code")
    val errorCode: String? = null,
    @Column("error_message")
    val errorMessage: String? = null,
    @Column("created_at")
    val createdAt: LocalDateTime? = null
)