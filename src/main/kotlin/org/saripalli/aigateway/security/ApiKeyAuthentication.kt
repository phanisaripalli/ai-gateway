package org.saripalli.aigateway.security

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority

class ApiKeyAuthentication(
    val gatewayContext: GatewayContext
) : AbstractAuthenticationToken(listOf(SimpleGrantedAuthority("ROLE_API_USER"))) {

    init {
        isAuthenticated = true
    }

    override fun getCredentials(): Any? = null

    override fun getPrincipal(): GatewayContext = gatewayContext
}
