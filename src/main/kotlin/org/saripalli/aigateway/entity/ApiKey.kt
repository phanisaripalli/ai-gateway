package org.saripalli.aigateway.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("api_keys")
data class ApiKey(
    @Id
    val id: UUID? = null,
    @Column("project_id")
    val projectId: UUID,
    @Column("key_prefix")
    val keyPrefix: String,
    @Column("key_hash")
    val keyHash: String,
    val name: String? = null,
    @Column("rate_limit_rpm")
    val rateLimitRpm: Int = 60,
    @Column("is_active")
    val isActive: Boolean = true,
    @Column("created_at")
    val createdAt: LocalDateTime? = null,
    @Column("last_used_at")
    val lastUsedAt: LocalDateTime? = null
)