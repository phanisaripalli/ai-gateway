package org.saripalli.aigateway.provider.gemini

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

// --- Request Models ---

data class GeminiGenerateContentRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

data class GeminiContent(
    val role: String,
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiGenerationConfig(
    val temperature: Double? = null,
    val maxOutputTokens: Int? = null
)

// --- Response Models ---

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate>? = null,
    val usageMetadata: GeminiUsageMetadata? = null,
    val error: GeminiError? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiUsageMetadata(
    val promptTokenCount: Int = 0,
    val candidatesTokenCount: Int = 0,
    val totalTokenCount: Int = 0
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)