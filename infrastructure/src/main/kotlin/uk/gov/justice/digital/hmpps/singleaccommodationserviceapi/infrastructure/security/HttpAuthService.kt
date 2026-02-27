package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

@Service
class HttpAuthService {
  fun getPrincipalOrThrow(acceptableSources: List<String>): UserPrincipal {
    val authToken = SecurityContextHolder.getContext().authentication as AuthAwareAuthenticationToken
    if (!acceptableSources.contains(authToken.principal.authSource.value)) {
      throw AccessDeniedException("JWT token does not contain auth_source claim")
    }
    return authToken.principal
  }

  fun getJwt(): Jwt {
    val principal = SecurityContextHolder.getContext().authentication as AuthAwareAuthenticationToken
    return principal.token
  }
}
