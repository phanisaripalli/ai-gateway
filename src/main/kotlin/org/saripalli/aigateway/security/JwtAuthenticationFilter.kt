package org.saripalli.aigateway.security

import org.springframework.http.HttpCookie
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.path.value()

        // Only apply JWT auth for dashboard and management API paths
        if (!path.startsWith("/dashboard") && !path.startsWith("/api/v1/")) {
            return chain.filter(exchange)
        }

        // Login endpoints are public
        if (path == "/api/v1/auth/login" || path == "/api/v1/auth/logout") {
            return chain.filter(exchange)
        }

        val token = extractToken(exchange) ?: return chain.filter(exchange)

        val claims = jwtService.validateToken(token) ?: return chain.filter(exchange)

        val auth = UsernamePasswordAuthenticationToken(
            claims.subject, // admin ID
            null,
            listOf(SimpleGrantedAuthority("ROLE_ADMIN"))
        )

        return chain.filter(exchange)
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
    }

    private fun extractToken(exchange: ServerWebExchange): String? {
        // 1. Check AUTH_TOKEN cookie
        val cookie: HttpCookie? = exchange.request.cookies.getFirst(JwtService.AUTH_COOKIE)
        if (cookie != null && cookie.value.isNotBlank()) {
            return cookie.value
        }

        // 2. Check Authorization header (but not gw_ keys — those are for ApiKeyFilter)
        val authHeader = exchange.request.headers.getFirst("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ") && !authHeader.startsWith("Bearer gw_")) {
            return authHeader.removePrefix("Bearer ").trim()
        }

        return null
    }
}