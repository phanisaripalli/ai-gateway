package org.saripalli.aigateway.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("AI Gateway API")
                    .description(
                        """
                        Unified API gateway for multiple AI providers (Gemini, OpenAI, Anthropic).

                        ## Authentication

                        - **Gateway API** (`/v1/*`): Use Bearer token with your gateway API key (e.g., `gw_xxx...`)
                        - **Management API** (`/api/v1/*`): Use Bearer token with admin JWT

                        ## Capability Routing

                        Use `capability` instead of `model` to let the gateway choose the best model:
                        - `fast` - Optimized for speed
                        - `balanced` - Balance of speed and quality
                        - `thinking` - Extended reasoning
                        - `best` - Highest quality output
                        """.trimIndent()
                    )
                    .version("1.0.0")
                    .license(License().name("MIT").url("https://opensource.org/licenses/MIT"))
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        "gateway-key",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .description("Gateway API key (starts with gw_)")
                    )
                    .addSecuritySchemes(
                        "admin-jwt",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Admin JWT token")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList("gateway-key"))
            .tags(
                listOf(
                    Tag().name("Chat").description("Chat completions API (OpenAI-compatible)"),
                    Tag().name("Projects").description("Project management"),
                    Tag().name("API Keys").description("Gateway API key management"),
                    Tag().name("Credentials").description("Provider credential management"),
                    Tag().name("Stats").description("Usage statistics and dashboard data"),
                    Tag().name("Auth").description("Authentication endpoints")
                )
            )
    }
}
