package org.saripalli.aigateway.repository

import org.saripalli.aigateway.entity.Admin
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.util.UUID

interface AdminRepository : ReactiveCrudRepository<Admin, UUID> {

    fun findByEmail(email: String): Mono<Admin>
}