package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SecurityScheme(
  name = "bearer-jwt",
  type = SecuritySchemeType.HTTP,
  scheme = "bearer",
  bearerFormat = "JWT",
)
@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {
  private val version: String? = buildProperties.version

  @Bean
  fun customOpenAPI(@Value($$"${spring.profiles.active:current}") envName: String): OpenAPI = OpenAPI()
    .servers(listOf(Server().description(envName)))
    .info(
      Info().title("HMPPS Single Accommodation Service Api").version(version)
        .contact(Contact().name("HMPPS Digital Studio").email("feedback@digital.justice.gov.uk")),
    )
    .addSecurityItem(SecurityRequirement().addList("bearer-jwt"))
}
