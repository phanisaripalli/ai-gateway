package org.saripalli.aigateway.service

import org.saripalli.aigateway.dto.ApiKeyCreatedResponse
import org.saripalli.aigateway.dto.ApiKeyResponse
import org.saripalli.aigateway.dto.CreateApiKeyRequest
import org.saripalli.aigateway.entity.ApiKey
import org.saripalli.aigateway.repository.ApiKeyRepository
import org.saripalli.aigateway.repository.ProjectRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

@Service
class ApiKeyService(
    private val apiKeyRepository: ApiKeyRepository,
    private val projectRepository: ProjectRepository
) {

    private val secureRandom = SecureRandom()
    private val alphanumeric = "abcdefghijklmnopqrstuvwxyz0123456789"

    fun createKey(projectId: UUID, request: CreateApiKeyRequest): Mono<ApiKeyCreatedResponse> {
        return projectRepository.findById(projectId)
            .switchIfEmpty(Mono.error(NoSuchElementException("Project not found: $projectId")))
            .flatMap { project ->
                val projectHint = project.id.toString().take(5)
                val rawKey = generateRawKey(projectHint)
                val keyHash = sha256(rawKey)
                val keyPrefix = rawKey.take(12)

                val entity = ApiKey(
                    projectId = projectId,
                    keyPrefix = keyPrefix,
                    keyHash = keyHash,
                    name = request.name
                )

                apiKeyRepository.save(entity).map { saved ->
                    ApiKeyCreatedResponse(
                        id = saved.id!!,
                        name = saved.name,
                        keyPrefix = keyPrefix,
                        key = rawKey,
                        createdAt = saved.createdAt
                    )
                }
            }
    }

    fun listKeys(projectId: UUID): Flux<ApiKeyResponse> {
        return apiKeyRepository.findByProjectIdAndIsActiveTrue(projectId)
            .map { it.toResponse() }
    }

    fun revokeKey(projectId: UUID, keyId: UUID): Mono<Void> {
        return apiKeyRepository.findById(keyId)
            .switchIfEmpty(Mono.error(NoSuchElementException("API key not found: $keyId")))
            .flatMap { key ->
                if (key.projectId != projectId) {
                    Mono.error(IllegalArgumentException("API key does not belong to project $projectId"))
                } else {
                    apiKeyRepository.save(key.copy(isActive = false))
                }
            }
            .then()
    }

    fun validateKey(rawKey: String): Mono<ApiKey> {
        val keyHash = sha256(rawKey)
        return apiKeyRepository.findByKeyHash(keyHash)
            .filter { it.isActive }
    }

    private fun generateRawKey(projectHint: String): String {
        val random = buildString {
            repeat(32) {
                append(alphanumeric[secureRandom.nextInt(alphanumeric.length)])
            }
        }
        return "gw_${projectHint}_$random"
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun ApiKey.toResponse() = ApiKeyResponse(
        id = id!!,
        name = name,
        keyPrefix = keyPrefix,
        rateLimitRpm = rateLimitRpm,
        isActive = isActive,
        createdAt = createdAt,
        lastUsedAt = lastUsedAt
    )
}