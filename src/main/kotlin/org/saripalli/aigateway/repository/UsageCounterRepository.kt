package org.saripalli.aigateway.repository

import org.saripalli.aigateway.entity.UsageCounter
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Repository
class UsageCounterRepository(
    private val databaseClient: DatabaseClient
) {

    fun findByProjectIdAndDate(projectId: UUID, date: LocalDate): Mono<UsageCounter> {
        return databaseClient.sql(
            "SELECT project_id, date, total_tokens, total_cost_usd, request_count " +
                "FROM usage_counters WHERE project_id = :projectId AND date = :date"
        )
            .bind("projectId", projectId)
            .bind("date", date)
            .map { row, _ ->
                UsageCounter(
                    projectId = row.get("project_id", UUID::class.java)!!,
                    date = row.get("date", LocalDate::class.java)!!,
                    totalTokens = row.get("total_tokens", java.lang.Long::class.java)?.toLong() ?: 0,
                    totalCostUsd = row.get("total_cost_usd", BigDecimal::class.java) ?: BigDecimal.ZERO,
                    requestCount = row.get("request_count", java.lang.Integer::class.java)?.toInt() ?: 0
                )
            }
            .one()
    }

    fun incrementUsage(projectId: UUID, date: LocalDate, tokens: Long, costUsd: BigDecimal): Mono<Void> {
        return databaseClient.sql(
            """
            INSERT INTO usage_counters (project_id, date, total_tokens, total_cost_usd, request_count)
            VALUES (:projectId, :date, :tokens, :cost, 1)
            ON CONFLICT (project_id, date)
            DO UPDATE SET
                total_tokens = usage_counters.total_tokens + :tokens,
                total_cost_usd = usage_counters.total_cost_usd + :cost,
                request_count = usage_counters.request_count + 1
            """.trimIndent()
        )
            .bind("projectId", projectId)
            .bind("date", date)
            .bind("tokens", tokens)
            .bind("cost", costUsd)
            .then()
    }
}