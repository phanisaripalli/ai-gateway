package org.saripalli.aigateway.security

import org.saripalli.aigateway.entity.Admin
import org.saripalli.aigateway.repository.AdminRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class AdminInitializer(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${admin.email:#{null}}")
    private val adminEmail: String?,
    @Value("\${admin.password:#{null}}")
    private val adminPassword: String?
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(AdminInitializer::class.java)

    override fun run(args: ApplicationArguments?) {
        adminRepository.count()
            .flatMap { count ->
                if (count == 0L) {
                    val email = adminEmail
                    val password = adminPassword
                    if (email.isNullOrBlank() || password.isNullOrBlank()) {
                        log.warn("No admin account exists and ADMIN_EMAIL/ADMIN_PASSWORD not set. Dashboard will be inaccessible.")
                        return@flatMap reactor.core.publisher.Mono.empty()
                    }

                    val admin = Admin(
                        email = email,
                        passwordHash = passwordEncoder.encode(password)
                    )
                    adminRepository.save(admin)
                        .doOnSuccess { log.info("Admin account created for: {}", email) }
                } else {
                    log.info("Admin account already exists ({} found), skipping initialization.", count)
                    reactor.core.publisher.Mono.empty()
                }
            }
            .block() // Safe to block during startup
    }
}