package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.client

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.getOrNullWhenNotFound
import java.nio.charset.StandardCharsets

class RestClientResponseExceptionExtensionsTest {

  @Test
  fun `returns null when upstream returns 404`() {
    val result = getOrNullWhenNotFound<Any> {
      throw http404()
    }

    assertThat(result).isNull()
  }

  @Test
  fun `rethrows other 4xx client errors`() {
    assertThatThrownBy {
      getOrNullWhenNotFound<Any> {
        throw http403()
      }
    }.isInstanceOf(WebClientResponseException::class.java)
      .hasMessageContaining("Forbidden")
  }

  @Test
  fun `rethrows server errors`() {
    assertThatThrownBy {
      getOrNullWhenNotFound<Any> {
        throw http500()
      }
    }.isInstanceOf(WebClientResponseException::class.java)
      .hasMessageContaining("Internal Server Error")
  }

  private fun http404(): WebClientResponseException = WebClientResponseException.create(
    404,
    "Not Found",
    HttpHeaders.EMPTY,
    ByteArray(0),
    StandardCharsets.UTF_8,
  )

  private fun http403(): WebClientResponseException = WebClientResponseException.create(
    403,
    "Forbidden",
    HttpHeaders.EMPTY,
    ByteArray(0),
    StandardCharsets.UTF_8,
  )

  private fun http500(): WebClientResponseException = WebClientResponseException.create(
    500,
    "Internal Server Error",
    HttpHeaders.EMPTY,
    ByteArray(0),
    StandardCharsets.UTF_8,
  )
}
