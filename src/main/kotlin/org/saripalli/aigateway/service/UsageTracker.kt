package org.saripalli.aigateway.service

import org.saripalli.aigateway.repository.ProjectRepository
import org.saripalli.aigateway.repository.UsageCounterRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Service
class UsageTracker(
    private val projectRepository: ProjectRepository,
    private val usageCounterRepository: UsageCounterRepository
) {

    /**
     * Checks if the project has exceeded its daily limits.
     * Returns Mono<Void> if OK, or Mono.error(LimitExceededException) if blocked.
     */
    fun checkLimits(projectId: UUID, estimatedTokens: Int, estimatedCost: BigDecimal): Mono<Void> {
        return projectRepository.findById(projectId)
            .flatMap { project ->
                val today = LocalDate.now()

                usageCounterRepository.findByProjectIdAndDate(projectId, today)
                    .defaultIfEmpty(emptyCounter(projectId, today))
                    .flatMap { usage ->
                        // Check daily cost limit
                        val dailyCostLimit = project.dailyCostLimit
                        if (dailyCostLimit != null) {
                            val projectedCost = usage.totalCostUsd.add(estimatedCost)
                            if (projectedCost > dailyCostLimit) {
                                return@flatMap Mono.error<Void>(
                                    LimitExceededException(
                                        "Daily cost limit exceeded. " +
                                            "Limit: $$dailyCostLimit, " +
                                            "Current: $${usage.totalCostUsd}, " +
                                            "Estimated: $$estimatedCost"
                                    )
                                )
                            }
                        }

                        // Check daily token limit
                        val dailyTokenLimit = project.dailyTokenLimit
                        if (dailyTokenLimit != null) {
                            val projectedTokens = usage.totalTokens + estimatedTokens
                            if (projectedTokens > dailyTokenLimit) {
                                return@flatMap Mono.error<Void>(
                                    LimitExceededException(
                                        "Daily token limit exceeded. " +
                                            "Limit: $dailyTokenLimit, " +
                                            "Current: ${usage.totalTokens}, " +
                                            "Estimated: $estimatedTokens"
                                    )
                                )
                            }
                        }

                        // Check monthly cost limit
                        val monthlyCostLimit = project.monthlyCostLimit
                        // Monthly checks would need a monthly aggregation query;
                        // for now we rely on daily limits. Monthly can be added later
                        // with a sum query over the current month's usage_counters.

                        Mono.empty<Void>()
                    }
            }
    }

    /**
     * Records usage after a successful request.
     */
    fun recordUsage(projectId: UUID, tokens: Long, costUsd: BigDecimal): Mono<Void> {
        return usageCounterRepository.incrementUsage(projectId, LocalDate.now(), tokens, costUsd)
    }

    private fun emptyCounter(projectId: UUID, date: LocalDate) =
        org.saripalli.aigateway.entity.UsageCounter(
            projectId = projectId,
            date = date
        )
}