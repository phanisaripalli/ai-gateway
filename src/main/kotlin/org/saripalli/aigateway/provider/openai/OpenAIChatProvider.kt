package org.saripalli.aigateway.provider.openai

import org.saripalli.aigateway.service.ProviderCredentialService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class OpenAIChatProvider(
    private val openaiWebClient: WebClient,
    private val openaiConfig: OpenAIConfig,
    private val credentialService: ProviderCredentialService
) : OpenAICompatibleProvider() {

    override fun getProviderName(): String = "openai"

    override fun webClient(): WebClient = openaiWebClient

    override fun getApiKey(projectId: UUID): Mono<String> {
        return credentialService.getDecryptedKey(projectId, "openai")
            .switchIfEmpty(Mono.defer {
                try {
                    Mono.just(openaiConfig.getApiKey())
                } catch (e: Exception) {
                    Mono.error(e)
                }
            })
    }
}