package org.saripalli.aigateway.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Stats", description = "Usage statistics and dashboard data")
@SecurityRequirement(name = "admin-jwt")
class DashboardController(
    private val dashboardService: DashboardService
) {

    @Operation(summary = "Get overview statistics", description = "Returns request counts and costs for today and current month")
    @GetMapping("/overview")
    fun getOverview(): Mono<OverviewStats> {
        return dashboardService.getOverviewStats()
    }

    @Operation(summary = "Get daily costs", description = "Returns cost aggregates for each day in the period")
    @GetMapping("/costs")
    fun getCosts(@Parameter(description = "Number of days to include") @RequestParam(defaultValue = "30") days: Int): Flux<DailyCost> {
        return dashboardService.getCosts(days)
    }

    @Operation(summary = "Get provider usage", description = "Returns usage statistics grouped by provider")
    @GetMapping("/providers")
    fun getProviders(@Parameter(description = "Number of days to include") @RequestParam(defaultValue = "30") days: Int): Flux<ProviderStats> {
        return dashboardService.getProviderUsage(days)
    }

    @Operation(summary = "Get recent activity", description = "Returns the most recent API requests")
    @GetMapping("/activity")
    fun getActivity(@Parameter(description = "Maximum number of entries") @RequestParam(defaultValue = "50") limit: Int): Flux<ActivityEntry> {
        return dashboardService.getRecentActivity(limit)
    }
}
