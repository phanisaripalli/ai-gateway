package org.saripalli.aigateway.service

import org.springframework.stereotype.Component

@Component
class ProviderRegistry(providers: List<ChatProvider>) {

    private val providerMap: Map<String, ChatProvider> =
        providers.associateBy { it.getProviderName().lowercase() }

    fun getProvider(name: String): ChatProvider {
        return providerMap[name.lowercase()]
            ?: throw IllegalArgumentException(
                "Unknown provider: '$name'. Available providers: ${providerMap.keys}"
            )
    }

    fun availableProviders(): Set<String> = providerMap.keys
}