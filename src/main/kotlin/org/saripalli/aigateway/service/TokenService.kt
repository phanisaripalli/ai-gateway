package org.saripalli.aigateway.service

import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.EncodingType
import org.saripalli.aigateway.dto.ChatMessage
import org.springframework.stereotype.Service

@Service
class TokenService {

    private val registry = Encodings.newLazyEncodingRegistry()

    // cl100k_base (GPT-4 encoding) used as standard "gateway tokens" for all providers
    private val encoding = registry.getEncoding(EncodingType.CL100K_BASE)

    fun countTokens(text: String): Int {
        return encoding.countTokens(text)
    }

    fun countTokens(messages: List<ChatMessage>): Int {
        // Each message has overhead tokens for role/formatting (~4 per message + 2 for priming)
        val overhead = messages.size * 4 + 2
        val contentTokens = messages.sumOf { encoding.countTokens(it.role) + encoding.countTokens(it.content) }
        return contentTokens + overhead
    }
}