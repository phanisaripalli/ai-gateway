package org.saripalli.aigateway.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import reactor.core.publisher.Mono
import java.net.URI

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val apiKeyAuthenticationFilter: ApiKeyAuthenticationFilter,
    private val rateLimitFilter: RateLimitFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint(authenticationEntryPoint())
            }
            .authorizeExchange { auth ->
                auth
                    // Public endpoints
                    .pathMatchers("/health", "/ready").permitAll()
                    .pathMatchers("/actuator/**").permitAll()
                    .pathMatchers("/login", "/chat", "/api/v1/auth/login", "/api/v1/auth/logout").permitAll()
                    .pathMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                    // API Documentation
                    .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()
                    // Admin-only: dashboard UI and management APIs
                    .pathMatchers("/dashboard/**").hasRole("ADMIN")
                    .pathMatchers("/api/v1/**").hasRole("ADMIN")
                    // Gateway: API key authenticated
                    .pathMatchers("/v1/**").hasRole("API_USER")
                    // Everything else requires authentication
                    .anyExchange().authenticated()
            }
            // JWT filter runs first (for dashboard/management paths)
            .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            // API Key filter runs next (for gateway paths)
            .addFilterBefore(apiKeyAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            // Rate limiting after authentication
            .addFilterAfter(rateLimitFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }

    private fun authenticationEntryPoint(): ServerAuthenticationEntryPoint {
        return ServerAuthenticationEntryPoint { exchange, _ ->
            val path = exchange.request.path.value()
            if (path.startsWith("/api/") || path.startsWith("/v1/")) {
                // API requests get a JSON 401
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                exchange.response.headers.set("Content-Type", "application/json")
                val body = """{"error":{"message":"Authentication required","type":"authentication_error"}}"""
                val buffer = exchange.response.bufferFactory().wrap(body.toByteArray())
                exchange.response.writeWith(Mono.just(buffer))
            } else {
                // Dashboard requests get redirected to login
                exchange.response.statusCode = HttpStatus.FOUND
                exchange.response.headers.location = URI.create("/login")
                exchange.response.setComplete()
            }
        }
    }
}