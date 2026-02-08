package org.saripalli.aigateway.repository

import org.saripalli.aigateway.entity.Project
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.UUID

interface ProjectRepository : ReactiveCrudRepository<Project, UUID> {

    fun findByIsActiveTrue(): Flux<Project>
}