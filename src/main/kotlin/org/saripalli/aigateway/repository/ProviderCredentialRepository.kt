package org.saripalli.aigateway.repository

import org.saripalli.aigateway.entity.ProviderCredential
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface ProviderCredentialRepository : ReactiveCrudRepository<ProviderCredential, UUID> {

    fun findByProjectIdAndProvider(projectId: UUID, provider: String): Mono<ProviderCredential>

    fun findByProjectId(projectId: UUID): Flux<ProviderCredential>
}