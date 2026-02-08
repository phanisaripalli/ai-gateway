package org.saripalli.aigateway.security

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class RateLimitService(
    @Value("\${rate-limit.default-rpm:60}")
    private val defaultRpm: Long
) {

    private val buckets = ConcurrentHashMap<UUID, Bucket>()

    /**
     * Returns the bucket for the given API key, creating one if it doesn't exist.
     */
    fun resolveBucket(apiKeyId: UUID, rateLimitRpm: Int?): Bucket {
        return buckets.computeIfAbsent(apiKeyId) {
            createBucket(rateLimitRpm?.toLong() ?: defaultRpm)
        }
    }

    /**
     * Attempts to consume one token from the bucket.
     * Returns a RateLimitResult with the outcome and current state.
     */
    fun tryConsume(apiKeyId: UUID, rateLimitRpm: Int?): RateLimitResult {
        val bucket = resolveBucket(apiKeyId, rateLimitRpm)
        val probe = bucket.tryConsumeAndReturnRemaining(1)
        val rpm = rateLimitRpm?.toLong() ?: defaultRpm

        return RateLimitResult(
            allowed = probe.isConsumed,
            limit = rpm,
            remaining = probe.remainingTokens,
            retryAfterNanos = if (probe.isConsumed) 0 else probe.nanosToWaitForRefill
        )
    }

    private fun createBucket(rpm: Long): Bucket {
        val bandwidth = Bandwidth.builder()
            .capacity(rpm)
            .refillGreedy(rpm, Duration.ofMinutes(1))
            .build()
        return Bucket.builder()
            .addLimit(bandwidth)
            .build()
    }
}

data class RateLimitResult(
    val allowed: Boolean,
    val limit: Long,
    val remaining: Long,
    val retryAfterNanos: Long
) {
    val retryAfterSeconds: Long
        get() = if (retryAfterNanos > 0) (retryAfterNanos / 1_000_000_000) + 1 else 0
}