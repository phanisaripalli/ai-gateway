package org.saripalli.aigateway.service

import org.saripalli.aigateway.dto.CreateProjectRequest
import org.saripalli.aigateway.dto.ProjectResponse
import org.saripalli.aigateway.entity.Project
import org.saripalli.aigateway.repository.ProjectRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class ProjectService(
    private val projectRepository: ProjectRepository
) {

    fun createProject(request: CreateProjectRequest): Mono<ProjectResponse> {
        require(request.name.isNotBlank()) { "Project name must not be empty" }

        val project = Project(
            name = request.name,
            description = request.description,
            dailyTokenLimit = request.dailyTokenLimit,
            dailyCostLimit = request.dailyCostLimit,
            monthlyTokenLimit = request.monthlyTokenLimit,
            monthlyCostLimit = request.monthlyCostLimit,
            defaultProvider = request.defaultProvider,
            defaultCapability = request.defaultCapability ?: "balanced"
        )

        return projectRepository.save(project).map { it.toResponse() }
    }

    fun getProject(id: UUID): Mono<ProjectResponse> {
        return projectRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Project not found: $id")))
            .map { it.toResponse() }
    }

    fun listProjects(): Flux<ProjectResponse> {
        return projectRepository.findByIsActiveTrue()
            .map { it.toResponse() }
    }

    fun deleteProject(id: UUID): Mono<Void> {
        return projectRepository.findById(id)
            .switchIfEmpty(Mono.error(NoSuchElementException("Project not found: $id")))
            .flatMap { project ->
                projectRepository.save(project.copy(isActive = false))
            }
            .then()
    }

    private fun Project.toResponse() = ProjectResponse(
        id = id!!,
        name = name,
        description = description,
        dailyTokenLimit = dailyTokenLimit,
        dailyCostLimit = dailyCostLimit,
        monthlyTokenLimit = monthlyTokenLimit,
        monthlyCostLimit = monthlyCostLimit,
        defaultProvider = defaultProvider,
        defaultCapability = defaultCapability,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}