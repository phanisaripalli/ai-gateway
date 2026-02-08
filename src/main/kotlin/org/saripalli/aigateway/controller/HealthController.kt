package org.saripalli.aigateway.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class HealthController {

    @GetMapping("/health")
    fun health(): Mono<String> {
        return Mono.just("OK")
    }

    @GetMapping("/ready")
    fun ready(): Mono<String> {
        return Mono.just("READY")
    }
}