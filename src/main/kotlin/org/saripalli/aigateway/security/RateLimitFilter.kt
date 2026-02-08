package org.saripalli.aigateway.security

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class RateLimitFilter(
    private val rateLimitService: RateLimitService
) : WebFilter {

    private val log = LoggerFactory.getLogger(RateLimitFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.path.value()

        // Only rate-limit gateway endpoints
        if (!path.startsWith("/v1/")) {
            return chain.filter(exchange)
        }

        return ReactiveSecurityContextHolder.getContext()
            .flatMap { securityContext ->
                val auth = securityContext.authentication
                if (auth !is ApiKeyAuthentication) {
                    return@flatMap chain.filter(exchange)
                }

                val ctx = auth.gatewayContext
                // rateLimitRpm from the API key entity is not stored in GatewayContext,
                // so we use the default. Per-key RPM can be added by extending GatewayContext.
                val result = rateLimitService.tryConsume(ctx.apiKeyId, null)

                // Always add rate limit headers
                exchange.response.headers.set("X-RateLimit-Limit", result.limit.toString())
                exchange.response.headers.set("X-RateLimit-Remaining", result.remaining.toString())

                if (result.allowed) {
                    chain.filter(exchange)
                } else {
                    log.warn("Rate limit exceeded for apiKey={}, project={}", ctx.apiKeyId, ctx.projectId)
                    exchange.response.headers.set("Retry-After", result.retryAfterSeconds.toString())
                    tooManyRequests(exchange)
                }
            }
            .switchIfEmpty(chain.filter(exchange))
    }

    private fun tooManyRequests(exchange: ServerWebExchange): Mono<Void> {
        exchange.response.statusCode = HttpStatus.TOO_MANY_REQUESTS
        exchange.response.headers.set("Content-Type", "application/json")
        val body = """{"error":{"message":"Rate limit exceeded. Please retry after the Retry-After period.","type":"rate_limit_error"}}"""
        val buffer = exchange.response.bufferFactory().wrap(body.toByteArray())
        return exchange.response.writeWith(Mono.just(buffer))
    }
}