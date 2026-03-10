package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.Username

class AuthAwareAuthenticationTokenTest {

  @Test
  fun `username is created in uppercase`() {
    val username = Username("username")
    assertThat(username.value).isUpperCase()
  }
}
