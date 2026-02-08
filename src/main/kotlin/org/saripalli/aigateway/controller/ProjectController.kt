package org.saripalli.aigateway.controller

import org.saripalli.aigateway.dto.CreateProjectRequest
import org.saripalli.aigateway.dto.ProjectResponse
import org.saripalli.aigateway.service.ProjectService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/v1/projects")
class ProjectController(
    private val projectService: ProjectService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createProject(@RequestBody request: CreateProjectRequest): Mono<ProjectResponse> {
        return projectService.createProject(request)
    }

    @GetMapping
    fun listProjects(): Flux<ProjectResponse> {
        return projectService.listProjects()
    }

    @GetMapping("/{id}")
    fun getProject(@PathVariable id: UUID): Mono<ProjectResponse> {
        return projectService.getProject(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProject(@PathVariable id: UUID): Mono<Void> {
        return projectService.deleteProject(id)
    }
}