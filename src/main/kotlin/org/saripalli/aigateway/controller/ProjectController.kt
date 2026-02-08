package org.saripalli.aigateway.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Projects", description = "Project management")
@SecurityRequirement(name = "admin-jwt")
class ProjectController(
    private val projectService: ProjectService
) {

    @Operation(summary = "Create a new project")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Project created"),
        ApiResponse(responseCode = "400", description = "Invalid request")
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createProject(@RequestBody request: CreateProjectRequest): Mono<ProjectResponse> {
        return projectService.createProject(request)
    }

    @Operation(summary = "List all projects")
    @GetMapping
    fun listProjects(): Flux<ProjectResponse> {
        return projectService.listProjects()
    }

    @Operation(summary = "Get project by ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Project found"),
        ApiResponse(responseCode = "404", description = "Project not found")
    )
    @GetMapping("/{id}")
    fun getProject(@Parameter(description = "Project ID") @PathVariable id: UUID): Mono<ProjectResponse> {
        return projectService.getProject(id)
    }

    @Operation(summary = "Delete a project")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Project deleted"),
        ApiResponse(responseCode = "404", description = "Project not found")
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProject(@Parameter(description = "Project ID") @PathVariable id: UUID): Mono<Void> {
        return projectService.deleteProject(id)
    }
}