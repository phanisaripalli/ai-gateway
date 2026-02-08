package org.saripalli.aigateway.provider.openai

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class OpenAIConfig {

    @Value("\${openai.api-key:#{null}}")
    private val apiKey: String? = null

    @Value("\${openai.base-url:https://api.openai.com/v1}")
    private val baseUrl: String = "https://api.openai.com/v1"

    @Bean
    fun openaiWebClient(builder: WebClient.Builder): WebClient {
        return builder
            .baseUrl(baseUrl)
            .build()
    }

    fun getApiKey(): String {
        return apiKey ?: throw IllegalStateException(
            "OPENAI_API_KEY is not configured. Set it via environment variable or openai.api-key property."
        )
    }
}