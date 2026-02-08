package org.saripalli.aigateway.controller

import org.saripalli.aigateway.repository.AdminRepository
import org.saripalli.aigateway.security.JwtService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val email: String
)

data class ErrorResponse(
    val message: String
)

data class ChangePasswordRequest(
    val current_password: String,
    val new_password: String
)

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): Mono<ResponseEntity<Any>> {
        return adminRepository.findByEmail(request.email)
            .flatMap { admin ->
                if (passwordEncoder.matches(request.password, admin.passwordHash)) {
                    val token = jwtService.generateToken(admin)
                    val cookie = jwtService.createAuthCookie(token)
                    Mono.just(
                        ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, cookie.toString())
                            .body(LoginResponse(token, admin.email) as Any)
                    )
                } else {
                    Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ErrorResponse("Invalid credentials") as Any))
                }
            }
            .switchIfEmpty(
                Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("Invalid credentials") as Any))
            )
    }

    @PostMapping("/logout")
    fun logout(): Mono<ResponseEntity<Void>> {
        val cookie = jwtService.createLogoutCookie()
        return Mono.just(
            ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build()
        )
    }

    @PostMapping("/change-password")
    fun changePassword(
        @RequestBody request: ChangePasswordRequest,
        @CookieValue("AUTH_TOKEN", required = false) token: String?
    ): Mono<ResponseEntity<Any>> {
        if (token.isNullOrBlank()) {
            return Mono.just(
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("Not authenticated") as Any)
            )
        }

        val email = try {
            jwtService.extractEmail(token)
        } catch (e: Exception) {
            return Mono.just(
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("Invalid token") as Any)
            )
        }

        if (request.new_password.length < 8) {
            return Mono.just(
                ResponseEntity.badRequest()
                    .body(ErrorResponse("Password must be at least 8 characters") as Any)
            )
        }

        return adminRepository.findByEmail(email)
            .flatMap { admin ->
                if (!passwordEncoder.matches(request.current_password, admin.passwordHash)) {
                    Mono.just(
                        ResponseEntity.badRequest()
                            .body(ErrorResponse("Current password is incorrect") as Any)
                    )
                } else {
                    val newHash = passwordEncoder.encode(request.new_password)
                    adminRepository.save(admin.copy(passwordHash = newHash))
                        .map {
                            ResponseEntity.ok()
                                .body(mapOf("message" to "Password changed successfully") as Any)
                        }
                }
            }
            .switchIfEmpty(
                Mono.just(
                    ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse("Admin not found") as Any)
                )
            )
    }
}
