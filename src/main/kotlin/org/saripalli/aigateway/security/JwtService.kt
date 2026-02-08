package org.saripalli.aigateway.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.saripalli.aigateway.entity.Admin
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${jwt.secret}")
    private val jwtSecret: String,
    @Value("\${jwt.expiration-hours:24}")
    private val expirationHours: Long
) {

    companion object {
        const val AUTH_COOKIE = "AUTH_TOKEN"
    }

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray().copyOf(64)) // Ensure 512-bit key for HS512
    }

    fun generateToken(admin: Admin): String {
        val now = Date()
        val expiration = Date(now.time + Duration.ofHours(expirationHours).toMillis())

        return Jwts.builder()
            .subject(admin.id.toString())
            .claim("email", admin.email)
            .claim("role", "ADMIN")
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (_: Exception) {
            null
        }
    }

    fun extractEmail(token: String): String {
        val claims = validateToken(token)
            ?: throw IllegalArgumentException("Invalid token")
        return claims["email"] as? String
            ?: throw IllegalArgumentException("Email not found in token")
    }

    fun createAuthCookie(token: String, secure: Boolean = false): ResponseCookie {
        return ResponseCookie.from(AUTH_COOKIE, token)
            .httpOnly(true)
            .secure(secure)
            .path("/")
            .maxAge(Duration.ofHours(expirationHours))
            .sameSite("Lax")
            .build()
    }

    fun createLogoutCookie(): ResponseCookie {
        return ResponseCookie.from(AUTH_COOKIE, "")
            .httpOnly(true)
            .path("/")
            .maxAge(0)
            .build()
    }
}