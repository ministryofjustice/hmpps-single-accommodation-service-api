package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration

import io.swagger.v3.parser.OpenAPIV3Parser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class OpenApiDocsIT : IntegrationTestBase() {
  @LocalServerPort
  private val port: Int = 0

  @Test
  fun `open api docs are available`() {
    restTestClient.get()
      .uri("/swagger-ui/index.html?configUrl=/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `open api docs redirect to correct page`() {
    restTestClient.get()
      .uri("/swagger-ui.html")
      .accept(MediaType.APPLICATION_JSON)
      .exchangeSuccessfully()
  }

  @Test
  fun `the open api json contains documentation`() {
    restTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("paths").isNotEmpty
  }

  @Test
  fun `the open api json contains the version number`() {
    restTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("info.version").isEqualTo(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
  }

  @Test
  fun `the open api json is valid`() {
    val result = OpenAPIV3Parser().readLocation("http://localhost:$port/v3/api-docs", null, null)
    assertThat(result.messages).isEmpty()
  }

  @ParameterizedTest
  @CsvSource(value = ["bearer-jwt"])
  fun `the security scheme is setup for bearer tokens`(key: String) {
    restTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.components.securitySchemes.$key.type").isEqualTo("http")
      .jsonPath("$.components.securitySchemes.$key.scheme").isEqualTo("bearer")
      .jsonPath("$.components.securitySchemes.$key.bearerFormat").isEqualTo("JWT")
  }

  @Test
  fun `the project has a security scheme defined`() {
    restTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.security").isArray
      .jsonPath("$.security[0]['bearer-jwt']").isEmpty // empty because we have no scopes defined.
      .jsonPath("$.components.securitySchemes['bearer-jwt'].type").isEqualTo("http")
      .jsonPath("$.components.securitySchemes['bearer-jwt'].scheme").isEqualTo("bearer")
      .jsonPath("$.components.securitySchemes['bearer-jwt'].bearerFormat").isEqualTo("JWT")
  }
}
