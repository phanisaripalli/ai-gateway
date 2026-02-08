package org.saripalli.aigateway.service

import org.saripalli.aigateway.dto.ChatCompletionRequest
import org.saripalli.aigateway.dto.ChatCompletionResponse
import reactor.core.publisher.Mono
import java.util.UUID

interface ChatProvider {

    fun chat(request: ChatCompletionRequest, projectId: UUID): Mono<ChatCompletionResponse>

    fun getProviderName(): String
}