package org.saripalli.aigateway.repository

import org.saripalli.aigateway.entity.ApiKey
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface ApiKeyRepository : ReactiveCrudRepository<ApiKey, UUID> {

    fun findByProjectIdAndIsActiveTrue(projectId: UUID): Flux<ApiKey>

    fun findByKeyHash(keyHash: String): Mono<ApiKey>
}