package org.saripalli.aigateway.security

import java.util.UUID

/**
 * Holds the authenticated project and API key context for the current request.
 */
data class GatewayContext(
    val projectId: UUID,
    val apiKeyId: UUID,
    val defaultProvider: String? = null
)