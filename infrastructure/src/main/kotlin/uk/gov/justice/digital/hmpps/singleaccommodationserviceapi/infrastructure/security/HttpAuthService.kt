package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class HttpAuthService {
  fun getPrincipalOrThrow(acceptableSources: List<String>): AuthAwareAuthenticationToken {
    val principal = SecurityContextHolder.getContext().authentication as AuthAwareAuthenticationToken
    if (!acceptableSources.contains(principal.token.claims["auth_source"])) {
      throw AccessDeniedException("JWT token does not contain auth_source claim")
    }
    return principal
  }

  fun getDeliusPrincipalOrThrow(): AuthAwareAuthenticationToken = getPrincipalOrThrow(listOf("delius"))
}
