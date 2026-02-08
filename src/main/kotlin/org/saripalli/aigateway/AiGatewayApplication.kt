package org.saripalli.aigateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AiGatewayApplication

fun main(args: Array<String>) {
    runApplication<AiGatewayApplication>(*args)
}