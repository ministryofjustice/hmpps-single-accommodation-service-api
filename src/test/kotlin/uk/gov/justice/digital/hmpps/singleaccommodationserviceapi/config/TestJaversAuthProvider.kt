package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import org.javers.spring.auditable.AuthorProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.AuthAwareAuthenticationToken
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserPrincipal
import java.util.UUID

@TestConfiguration
class TestJaversAuthProvider(
  @Value($$"${test-data-setup.user-id}")
  private val testDataSetupUserId: UUID,
) : AuthorProvider {
  override fun provide(): String {
    try {
      val authToken = SecurityContextHolder.getContext().authentication as AuthAwareAuthenticationToken
      val userPrincipal = authToken.principal as UserPrincipal
      return userPrincipal.sasUserId.toString()
    } catch (_: Exception) {
      return testDataSetupUserId.toString()
    }
  }
}
