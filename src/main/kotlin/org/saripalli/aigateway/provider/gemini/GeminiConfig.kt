package org.saripalli.aigateway.provider.gemini

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class GeminiConfig {

    @Value("\${gemini.api-key:#{null}}")
    private val apiKey: String? = null

    @Value("\${gemini.base-url:https://generativelanguage.googleapis.com}")
    private val baseUrl: String = "https://generativelanguage.googleapis.com"

    @Bean
    fun geminiWebClient(builder: WebClient.Builder): WebClient {
        return builder
            .baseUrl(baseUrl)
            .build()
    }

    fun getApiKey(): String {
        return apiKey ?: throw IllegalStateException(
            "GEMINI_API_KEY is not configured. Set it via environment variable or gemini.api-key property."
        )
    }
}