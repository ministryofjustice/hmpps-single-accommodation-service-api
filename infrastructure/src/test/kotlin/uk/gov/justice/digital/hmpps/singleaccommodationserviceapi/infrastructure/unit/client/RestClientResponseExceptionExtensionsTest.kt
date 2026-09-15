package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.client

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
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
    }.isInstanceOf(HttpClientErrorException::class.java)
      .hasMessageContaining("Forbidden")
  }

  @Test
  fun `rethrows server errors`() {
    assertThatThrownBy {
      getOrNullWhenNotFound<Any> {
        throw http500()
      }
    }.isInstanceOf(HttpServerErrorException::class.java)
      .hasMessageContaining("Internal Server Error")
  }

  private fun http404(): HttpClientErrorException = HttpClientErrorException.create(
    HttpStatus.NOT_FOUND,
    "Not Found",
    HttpHeaders.EMPTY,
    ByteArray(0),
    StandardCharsets.UTF_8,
  )

  private fun http403(): HttpClientErrorException = HttpClientErrorException.create(
    HttpStatus.FORBIDDEN,
    "Forbidden",
    HttpHeaders.EMPTY,
    ByteArray(0),
    StandardCharsets.UTF_8,
  )

  private fun http500(): HttpServerErrorException = HttpServerErrorException.create(
    HttpStatus.INTERNAL_SERVER_ERROR,
    "Internal Server Error",
    HttpHeaders.EMPTY,
    ByteArray(0),
    StandardCharsets.UTF_8,
  )
}
