package org.saripalli.aigateway.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.util.UUID

data class CreateApiKeyRequest(
    val name: String? = null
)

data class ApiKeyCreatedResponse(
    val id: UUID,
    val name: String?,
    @JsonProperty("key_prefix")
    val keyPrefix: String,
    val key: String,
    val warning: String = "Copy this key now. It will not be shown again.",
    @JsonProperty("created_at")
    val createdAt: LocalDateTime?
)

data class ApiKeyResponse(
    val id: UUID,
    val name: String?,
    @JsonProperty("key_prefix")
    val keyPrefix: String,
    @JsonProperty("rate_limit_rpm")
    val rateLimitRpm: Int,
    @JsonProperty("is_active")
    val isActive: Boolean,
    @JsonProperty("created_at")
    val createdAt: LocalDateTime?,
    @JsonProperty("last_used_at")
    val lastUsedAt: LocalDateTime?
)