package org.saripalli.aigateway.security

import com.github.benmanes.caffeine.cache.Caffeine
import org.saripalli.aigateway.repository.ProjectRepository
import org.saripalli.aigateway.service.ApiKeyService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class ApiKeyAuthenticationFilter(
    private val apiKeyService: ApiKeyService,
    private val projectRepository: ProjectRepository
) : WebFilter {

    private val log = LoggerFactory.getLogger(ApiKeyAuthenticationFilter::class.java)

    // Cache: key hash -> GatewayContext (TTL 5 minutes)
    private val cache = Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterWrite(Duration.ofMinutes(5))
        .build<String, GatewayContext>()

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.path.value()

        // Skip auth for non-gateway paths
        if (!path.startsWith("/v1/")) {
            return chain.filter(exchange)
        }

        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer gw_")) {
            return unauthorized(exchange, "Missing or invalid Authorization header. Expected: Bearer gw_...")
        }

        val rawKey = authHeader.removePrefix("Bearer ").trim()

        // Check cache first
        val cached = cache.getIfPresent(rawKey)
        if (cached != null) {
            val auth = ApiKeyAuthentication(cached)
            return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
        }

        // Validate against DB
        return apiKeyService.validateKey(rawKey)
            .flatMap { apiKey ->
                projectRepository.findById(apiKey.projectId)
                    .filter { it.isActive }
                    .map { project ->
                        GatewayContext(
                            projectId = project.id!!,
                            apiKeyId = apiKey.id!!,
                            defaultProvider = project.defaultProvider
                        )
                    }
            }
            .flatMap { context ->
                cache.put(rawKey, context)
                val auth = ApiKeyAuthentication(context)
                chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
            }
            .switchIfEmpty(unauthorized(exchange, "Invalid or revoked API key"))
    }

    private fun unauthorized(exchange: ServerWebExchange, message: String): Mono<Void> {
        log.warn("Auth failed: {}", message)
        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
        exchange.response.headers.set("Content-Type", "application/json")
        val body = """{"error":{"message":"$message","type":"authentication_error"}}"""
        val buffer = exchange.response.bufferFactory().wrap(body.toByteArray())
        return exchange.response.writeWith(Mono.just(buffer))
    }
}