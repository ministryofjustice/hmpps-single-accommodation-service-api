package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.Username

class UsernameTest {

  @Test
  fun `username is created in uppercase`() {
    val username = Username("username")
    assertThat(username.value).isUpperCase()
  }

  @Test
  fun `blank string throws error`() {
    assertThrows<IllegalArgumentException> { Username("") }
  }

  @Test
  fun `toString() returns value`() {
    assertThat(Username("username").toString()).isEqualTo("USERNAME")
  }
}
