package org.saripalli.aigateway.repository

import org.saripalli.aigateway.dto.ProviderStats
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

data class ProviderUsage(
    val provider: String,
    val date: LocalDate,
    val totalTokens: Long = 0,
    val totalCost: BigDecimal = BigDecimal.ZERO,
    val requestCount: Int = 0
)

@Repository
class ProviderUsageRepository(
    private val databaseClient: DatabaseClient
) {

    fun findByProviderAndDate(provider: String, date: LocalDate): Mono<ProviderUsage> {
        return databaseClient.sql(
            "SELECT provider, date, total_tokens, total_cost, request_count " +
                "FROM provider_usage WHERE provider = :provider AND date = :date"
        )
            .bind("provider", provider)
            .bind("date", date)
            .map { row, _ ->
                ProviderUsage(
                    provider = row.get("provider", String::class.java)!!,
                    date = row.get("date", LocalDate::class.java)!!,
                    totalTokens = row.get("total_tokens", java.lang.Long::class.java)?.toLong() ?: 0,
                    totalCost = row.get("total_cost", BigDecimal::class.java) ?: BigDecimal.ZERO,
                    requestCount = row.get("request_count", java.lang.Integer::class.java)?.toInt() ?: 0
                )
            }
            .one()
    }

    fun findByDate(date: LocalDate): Flux<ProviderUsage> {
        return databaseClient.sql(
            "SELECT provider, date, total_tokens, total_cost, request_count " +
                "FROM provider_usage WHERE date = :date"
        )
            .bind("date", date)
            .map { row, _ ->
                ProviderUsage(
                    provider = row.get("provider", String::class.java)!!,
                    date = row.get("date", LocalDate::class.java)!!,
                    totalTokens = row.get("total_tokens", java.lang.Long::class.java)?.toLong() ?: 0,
                    totalCost = row.get("total_cost", BigDecimal::class.java) ?: BigDecimal.ZERO,
                    requestCount = row.get("request_count", java.lang.Integer::class.java)?.toInt() ?: 0
                )
            }
            .all()
    }

    fun findByDateRange(startDate: LocalDate, endDate: LocalDate): Flux<ProviderUsage> {
        return databaseClient.sql(
            "SELECT provider, date, total_tokens, total_cost, request_count " +
                "FROM provider_usage WHERE date >= :startDate AND date <= :endDate"
        )
            .bind("startDate", startDate)
            .bind("endDate", endDate)
            .map { row, _ ->
                ProviderUsage(
                    provider = row.get("provider", String::class.java)!!,
                    date = row.get("date", LocalDate::class.java)!!,
                    totalTokens = row.get("total_tokens", java.lang.Long::class.java)?.toLong() ?: 0,
                    totalCost = row.get("total_cost", BigDecimal::class.java) ?: BigDecimal.ZERO,
                    requestCount = row.get("request_count", java.lang.Integer::class.java)?.toInt() ?: 0
                )
            }
            .all()
    }

    fun sumByDateRange(startDate: LocalDate, endDate: LocalDate): Flux<ProviderStats> {
        return databaseClient.sql(
            "SELECT provider, SUM(request_count) as request_count, SUM(total_cost) as total_cost, " +
                "SUM(total_tokens) as total_tokens FROM provider_usage " +
                "WHERE date >= :startDate AND date <= :endDate GROUP BY provider"
        )
            .bind("startDate", startDate)
            .bind("endDate", endDate)
            .map { row, _ ->
                ProviderStats(
                    provider = row.get("provider", String::class.java)!!,
                    requestCount = row.get("request_count", java.lang.Long::class.java)?.toLong() ?: 0,
                    totalCost = row.get("total_cost", BigDecimal::class.java) ?: BigDecimal.ZERO,
                    totalTokens = row.get("total_tokens", java.lang.Long::class.java)?.toLong() ?: 0
                )
            }
            .all()
    }

    fun incrementUsage(provider: String, date: LocalDate, tokens: Long, cost: BigDecimal): Mono<Void> {
        return databaseClient.sql(
            """
            INSERT INTO provider_usage (provider, date, total_tokens, total_cost, request_count)
            VALUES (:provider, :date, :tokens, :cost, 1)
            ON CONFLICT (provider, date)
            DO UPDATE SET
                total_tokens = provider_usage.total_tokens + :tokens,
                total_cost = provider_usage.total_cost + :cost,
                request_count = provider_usage.request_count + 1
            """.trimIndent()
        )
            .bind("provider", provider)
            .bind("date", date)
            .bind("tokens", tokens)
            .bind("cost", cost)
            .then()
    }
}