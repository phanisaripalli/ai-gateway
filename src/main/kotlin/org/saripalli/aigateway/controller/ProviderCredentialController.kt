package org.saripalli.aigateway.controller

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.saripalli.aigateway.service.ProviderCredentialResponse
import org.saripalli.aigateway.service.ProviderCredentialService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

data class AddCredentialRequest(
    val provider: String,
    @JsonProperty("api_key")
    val apiKey: String
)

@RestController
@RequestMapping("/api/v1/projects/{projectId}/credentials")
@Tag(name = "Credentials", description = "Provider credential management")
@SecurityRequirement(name = "admin-jwt")
class ProviderCredentialController(
    private val credentialService: ProviderCredentialService
) {

    @Operation(summary = "List provider credentials", description = "Returns all provider credentials for the project (keys are masked)")
    @GetMapping
    fun listCredentials(@Parameter(description = "Project ID") @PathVariable projectId: UUID): Flux<ProviderCredentialResponse> {
        return credentialService.listCredentials(projectId)
    }

    @Operation(summary = "Add provider credential", description = "Adds or updates an API key for a provider (gemini, openai, anthropic)")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Credential added"),
        ApiResponse(responseCode = "400", description = "Invalid provider")
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addCredential(
        @Parameter(description = "Project ID") @PathVariable projectId: UUID,
        @RequestBody request: AddCredentialRequest
    ): Mono<ProviderCredentialResponse> {
        return credentialService.addCredential(projectId, request.provider, request.apiKey)
    }

    @Operation(summary = "Delete provider credential")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Credential deleted"),
        ApiResponse(responseCode = "404", description = "Credential not found")
    )
    @DeleteMapping("/{provider}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCredential(
        @Parameter(description = "Project ID") @PathVariable projectId: UUID,
        @Parameter(description = "Provider name (gemini, openai, anthropic)") @PathVariable provider: String
    ): Mono<Void> {
        return credentialService.deleteCredential(projectId, provider)
    }
}
