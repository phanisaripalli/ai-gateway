package org.saripalli.aigateway.security

import org.springframework.security.core.context.ReactiveSecurityContextHolder
import reactor.core.publisher.Mono

object GatewayContextHolder {

    fun getContext(): Mono<GatewayContext> {
        return ReactiveSecurityContextHolder.getContext()
            .map { it.authentication }
            .filter { it is ApiKeyAuthentication }
            .map { (it as ApiKeyAuthentication).gatewayContext }
    }
}