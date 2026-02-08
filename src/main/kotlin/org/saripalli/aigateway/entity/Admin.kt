package org.saripalli.aigateway.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("admins")
data class Admin(
    @Id
    val id: UUID? = null,
    val email: String,
    @Column("password_hash")
    val passwordHash: String,
    @Column("created_at")
    val createdAt: LocalDateTime? = null,
    @Column("last_login_at")
    val lastLoginAt: LocalDateTime? = null
)