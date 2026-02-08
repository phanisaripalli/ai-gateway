package org.saripalli.aigateway.repository

import org.saripalli.aigateway.entity.Request
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.UUID

interface RequestRepository : ReactiveCrudRepository<Request, UUID> {

    fun findByProjectIdOrderByCreatedAtDesc(projectId: UUID): Flux<Request>
}