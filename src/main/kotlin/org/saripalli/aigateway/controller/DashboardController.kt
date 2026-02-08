package org.saripalli.aigateway.controller

import org.saripalli.aigateway.dto.ActivityEntry
import org.saripalli.aigateway.dto.DailyCost
import org.saripalli.aigateway.dto.OverviewStats
import org.saripalli.aigateway.dto.ProviderStats
import org.saripalli.aigateway.service.DashboardService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/stats")
class DashboardController(
    private val dashboardService: DashboardService
) {

    @GetMapping("/overview")
    fun getOverview(): Mono<OverviewStats> {
        return dashboardService.getOverviewStats()
    }

    @GetMapping("/costs")
    fun getCosts(@RequestParam(defaultValue = "30") days: Int): Flux<DailyCost> {
        return dashboardService.getCosts(days)
    }

    @GetMapping("/providers")
    fun getProviders(@RequestParam(defaultValue = "30") days: Int): Flux<ProviderStats> {
        return dashboardService.getProviderUsage(days)
    }

    @GetMapping("/activity")
    fun getActivity(@RequestParam(defaultValue = "50") limit: Int): Flux<ActivityEntry> {
        return dashboardService.getRecentActivity(limit)
    }
}
