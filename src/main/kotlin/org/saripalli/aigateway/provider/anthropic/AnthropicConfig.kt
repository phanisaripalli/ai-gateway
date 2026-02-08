package org.saripalli.aigateway.provider.anthropic

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class AnthropicConfig {

    @Value("\${anthropic.api-key:#{null}}")
    private val apiKey: String? = null

    @Value("\${anthropic.base-url:https://api.anthropic.com}")
    private val baseUrl: String = "https://api.anthropic.com"

    @Value("\${anthropic.api-version:2023-06-01}")
    private val apiVersion: String = "2023-06-01"

    @Bean
    fun anthropicWebClient(builder: WebClient.Builder): WebClient {
        return builder
            .baseUrl(baseUrl)
            .build()
    }

    fun getApiKey(): String {
        return apiKey ?: throw IllegalStateException(
            "ANTHROPIC_API_KEY is not configured. Set it via environment variable or anthropic.api-key property."
        )
    }

    fun getApiVersion(): String = apiVersion
}