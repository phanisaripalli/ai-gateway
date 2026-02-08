package org.saripalli.aigateway.controller

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
class ApiKeyController(
    private val apiKeyService: ApiKeyService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createKey(
        @PathVariable projectId: UUID,
        @RequestBody(required = false) request: CreateApiKeyRequest?
    ): Mono<ApiKeyCreatedResponse> {
        return apiKeyService.createKey(projectId, request ?: CreateApiKeyRequest())
    }

    @GetMapping
    fun listKeys(@PathVariable projectId: UUID): Flux<ApiKeyResponse> {
        return apiKeyService.listKeys(projectId)
    }

    @DeleteMapping("/{keyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun revokeKey(
        @PathVariable projectId: UUID,
        @PathVariable keyId: UUID
    ): Mono<Void> {
        return apiKeyService.revokeKey(projectId, keyId)
    }
}