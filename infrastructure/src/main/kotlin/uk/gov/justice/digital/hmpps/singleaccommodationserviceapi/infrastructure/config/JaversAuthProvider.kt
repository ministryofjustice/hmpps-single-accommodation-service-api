package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.javers.spring.auditable.AuthorProvider
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.AuthAwareAuthenticationToken
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserPrincipal

@Configuration
@Profile("local", "dev", "preprod", "prod")
class JaversAuthProvider : AuthorProvider {
  override fun provide(): String {
    val authToken = SecurityContextHolder.getContext().authentication as AuthAwareAuthenticationToken
    val userPrincipal = authToken.principal as UserPrincipal
    return userPrincipal.sasUserId.toString()
  }
}
