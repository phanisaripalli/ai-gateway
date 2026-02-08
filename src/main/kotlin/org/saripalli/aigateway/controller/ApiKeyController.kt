package org.saripalli.aigateway.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.saripalli.aigateway.dto.ApiKeyCreatedResponse
import org.saripalli.aigateway.dto.ApiKeyResponse
import org.saripalli.aigateway.dto.CreateApiKeyRequest
import org.saripalli.aigateway.service.ApiKeyService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/v1/projects/{projectId}/keys")
@Tag(name = "API Keys", description = "Gateway API key management")
@SecurityRequirement(name = "admin-jwt")
class ApiKeyController(
    private val apiKeyService: ApiKeyService
) {

    @Operation(summary = "Generate a new API key", description = "Creates a new gateway API key for the project. The full key is only returned once.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "API key created"),
        ApiResponse(responseCode = "404", description = "Project not found")
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createKey(
        @Parameter(description = "Project ID") @PathVariable projectId: UUID,
        @RequestBody(required = false) request: CreateApiKeyRequest?
    ): Mono<ApiKeyCreatedResponse> {
        return apiKeyService.createKey(projectId, request ?: CreateApiKeyRequest())
    }

    @Operation(summary = "List API keys", description = "Returns all API keys for the project (keys are masked)")
    @GetMapping
    fun listKeys(@Parameter(description = "Project ID") @PathVariable projectId: UUID): Flux<ApiKeyResponse> {
        return apiKeyService.listKeys(projectId)
    }

    @Operation(summary = "Revoke an API key")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "API key revoked"),
        ApiResponse(responseCode = "404", description = "API key not found")
    )
    @DeleteMapping("/{keyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun revokeKey(
        @Parameter(description = "Project ID") @PathVariable projectId: UUID,
        @Parameter(description = "API Key ID") @PathVariable keyId: UUID
    ): Mono<Void> {
        return apiKeyService.revokeKey(projectId, keyId)
    }
}