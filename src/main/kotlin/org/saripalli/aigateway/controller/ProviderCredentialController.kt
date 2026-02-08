package org.saripalli.aigateway.controller

import com.fasterxml.jackson.annotation.JsonProperty
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
class ProviderCredentialController(
    private val credentialService: ProviderCredentialService
) {

    @GetMapping
    fun listCredentials(@PathVariable projectId: UUID): Flux<ProviderCredentialResponse> {
        return credentialService.listCredentials(projectId)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addCredential(
        @PathVariable projectId: UUID,
        @RequestBody request: AddCredentialRequest
    ): Mono<ProviderCredentialResponse> {
        return credentialService.addCredential(projectId, request.provider, request.apiKey)
    }

    @DeleteMapping("/{provider}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCredential(
        @PathVariable projectId: UUID,
        @PathVariable provider: String
    ): Mono<Void> {
        return credentialService.deleteCredential(projectId, provider)
    }
}
