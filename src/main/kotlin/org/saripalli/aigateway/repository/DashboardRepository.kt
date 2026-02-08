package org.saripalli.aigateway.repository

import org.saripalli.aigateway.dto.ActivityEntry
import org.saripalli.aigateway.dto.DailyCost
import org.saripalli.aigateway.dto.ModelStats
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Repository
class DashboardRepository(
    private val databaseClient: DatabaseClient
) {

    fun getTodayRequestCount(): Mono<Long> {
        return databaseClient.sql(
            "SELECT COALESCE(SUM(request_count), 0) as total FROM provider_usage WHERE date = CURRENT_DATE"
        )
            .map { row, _ -> row.get("total", java.lang.Long::class.java)?.toLong() ?: 0L }
            .one()
    }

    fun getMonthRequestCount(): Mono<Long> {
        return databaseClient.sql(
            "SELECT COALESCE(SUM(request_count), 0) as total FROM provider_usage WHERE date >= date_trunc('month', CURRENT_DATE)"
        )
            .map { row, _ -> row.get("total", java.lang.Long::class.java)?.toLong() ?: 0L }
            .one()
    }

    fun getTodayCost(): Mono<BigDecimal> {
        return databaseClient.sql(
            "SELECT COALESCE(SUM(total_cost), 0) as total FROM provider_usage WHERE date = CURRENT_DATE"
        )
            .map { row, _ -> row.get("total", BigDecimal::class.java) ?: BigDecimal.ZERO }
            .one()
    }

    fun getMonthCost(): Mono<BigDecimal> {
        return databaseClient.sql(
            "SELECT COALESCE(SUM(total_cost), 0) as total FROM provider_usage WHERE date >= date_trunc('month', CURRENT_DATE)"
        )
            .map { row, _ -> row.get("total", BigDecimal::class.java) ?: BigDecimal.ZERO }
            .one()
    }

    fun getDailyCosts(days: Int): Flux<DailyCost> {
        return databaseClient.sql(
            "SELECT date, SUM(total_cost) as cost FROM provider_usage " +
                "WHERE date >= :startDate GROUP BY date ORDER BY date"
        )
            .bind("startDate", LocalDate.now().minusDays(days.toLong()))
            .map { row, _ ->
                DailyCost(
                    date = row.get("date", LocalDate::class.java)!!,
                    cost = row.get("cost", BigDecimal::class.java) ?: BigDecimal.ZERO
                )
            }
            .all()
    }

    fun getTopModels(limit: Int): Flux<ModelStats> {
        return databaseClient.sql(
            "SELECT model, provider, COUNT(*) as request_count FROM requests " +
                "GROUP BY model, provider ORDER BY request_count DESC LIMIT :limit"
        )
            .bind("limit", limit)
            .map { row, _ ->
                ModelStats(
                    model = row.get("model", String::class.java)!!,
                    provider = row.get("provider", String::class.java)!!,
                    requestCount = row.get("request_count", java.lang.Long::class.java)?.toLong() ?: 0
                )
            }
            .all()
    }

    fun getRecentRequests(limit: Int): Flux<ActivityEntry> {
        return databaseClient.sql(
            "SELECT id, provider, model, status, cost_usd, " +
                "(input_tokens + output_tokens + thinking_tokens) as tokens, " +
                "latency_ms, created_at FROM requests ORDER BY created_at DESC LIMIT :limit"
        )
            .bind("limit", limit)
            .map { row, _ ->
                ActivityEntry(
                    id = row.get("id", UUID::class.java)!!,
                    provider = row.get("provider", String::class.java)!!,
                    model = row.get("model", String::class.java)!!,
                    status = row.get("status", String::class.java)!!,
                    cost = row.get("cost_usd", BigDecimal::class.java) ?: BigDecimal.ZERO,
                    tokens = row.get("tokens", java.lang.Integer::class.java)?.toInt() ?: 0,
                    latencyMs = row.get("latency_ms", java.lang.Integer::class.java)?.toInt(),
                    createdAt = row.get("created_at", LocalDateTime::class.java)
                )
            }
            .all()
    }
}
