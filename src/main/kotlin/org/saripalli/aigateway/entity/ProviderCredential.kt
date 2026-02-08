package org.saripalli.aigateway.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("provider_credentials")
data class ProviderCredential(
    @Id
    val id: UUID? = null,
    @Column("project_id")
    val projectId: UUID,
    val provider: String,
    @Column("encrypted_key")
    val encryptedKey: String,
    @Column("created_at")
    val createdAt: LocalDateTime? = null
)