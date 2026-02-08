package org.saripalli.aigateway.service

import org.saripalli.aigateway.entity.ProviderCredential
import org.saripalli.aigateway.repository.ProviderCredentialRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

data class ProviderCredentialResponse(
    val id: UUID,
    val provider: String,
    val maskedKey: String,
    val createdAt: String?
)

@Service
class ProviderCredentialService(
    private val credentialRepository: ProviderCredentialRepository,
    private val encryptionService: EncryptionService
) {

    companion object {
        val SUPPORTED_PROVIDERS = listOf("openai", "anthropic", "gemini")
    }

    fun addCredential(projectId: UUID, provider: String, apiKey: String): Mono<ProviderCredentialResponse> {
        val normalizedProvider = provider.lowercase()
        require(normalizedProvider in SUPPORTED_PROVIDERS) {
            "Unsupported provider: $provider. Supported: ${SUPPORTED_PROVIDERS.joinToString()}"
        }
        require(apiKey.isNotBlank()) { "API key must not be empty" }

        val encryptedKey = encryptionService.encrypt(apiKey)
        val credential = ProviderCredential(
            projectId = projectId,
            provider = normalizedProvider,
            encryptedKey = encryptedKey
        )

        return credentialRepository.findByProjectIdAndProvider(projectId, normalizedProvider)
            .flatMap { existing ->
                // Update existing credential
                credentialRepository.save(existing.copy(encryptedKey = encryptedKey))
            }
            .switchIfEmpty(credentialRepository.save(credential))
            .map { it.toResponse(apiKey) }
    }

    fun listCredentials(projectId: UUID): Flux<ProviderCredentialResponse> {
        return credentialRepository.findByProjectId(projectId)
            .map { credential ->
                val decryptedKey = try {
                    encryptionService.decrypt(credential.encryptedKey)
                } catch (e: Exception) {
                    "****"
                }
                credential.toResponse(decryptedKey)
            }
    }

    fun deleteCredential(projectId: UUID, provider: String): Mono<Void> {
        return credentialRepository.findByProjectIdAndProvider(projectId, provider.lowercase())
            .flatMap { credentialRepository.delete(it) }
    }

    fun getDecryptedKey(projectId: UUID, provider: String): Mono<String> {
        return credentialRepository.findByProjectIdAndProvider(projectId, provider.lowercase())
            .map { encryptionService.decrypt(it.encryptedKey) }
    }

    private fun ProviderCredential.toResponse(originalKey: String): ProviderCredentialResponse {
        val masked = if (originalKey.length > 8) {
            "${originalKey.take(4)}...${originalKey.takeLast(4)}"
        } else {
            "****"
        }
        return ProviderCredentialResponse(
            id = id!!,
            provider = provider,
            maskedKey = masked,
            createdAt = createdAt?.toString()
        )
    }
}
