package org.saripalli.aigateway.service

import org.saripalli.aigateway.dto.ActivityEntry
import org.saripalli.aigateway.dto.DailyCost
import org.saripalli.aigateway.dto.OverviewStats
import org.saripalli.aigateway.dto.ProviderStats
import org.saripalli.aigateway.repository.DashboardRepository
import org.saripalli.aigateway.repository.ProjectRepository
import org.saripalli.aigateway.repository.ProviderUsageRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

@Service
class DashboardService(
    private val dashboardRepository: DashboardRepository,
    private val providerUsageRepository: ProviderUsageRepository,
    private val projectRepository: ProjectRepository
) {

    fun getOverviewStats(): Mono<OverviewStats> {
        return Mono.zip(
            dashboardRepository.getTodayRequestCount(),
            dashboardRepository.getMonthRequestCount(),
            dashboardRepository.getTodayCost(),
            dashboardRepository.getMonthCost(),
            projectRepository.findByIsActiveTrue().count()
        ).map { tuple ->
            OverviewStats(
                requestsToday = tuple.t1,
                requestsMonth = tuple.t2,
                costToday = tuple.t3,
                costMonth = tuple.t4,
                activeProjects = tuple.t5
            )
        }
    }

    fun getCosts(days: Int): Flux<DailyCost> {
        return dashboardRepository.getDailyCosts(days)
    }

    fun getProviderUsage(days: Int): Flux<ProviderStats> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        return providerUsageRepository.sumByDateRange(startDate, endDate)
    }

    fun getRecentActivity(limit: Int): Flux<ActivityEntry> {
        return dashboardRepository.getRecentRequests(limit)
    }
}
