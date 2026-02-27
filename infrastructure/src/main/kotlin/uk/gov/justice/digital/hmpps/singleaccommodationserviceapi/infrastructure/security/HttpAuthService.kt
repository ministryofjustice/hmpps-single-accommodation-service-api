package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

@Service
class HttpAuthService {
  fun getPrincipalOrThrow(acceptableSources: List<String>): Pair<String, String> {
    val principal = SecurityContextHolder.getContext().authentication as AuthAwareAuthenticationToken
    val authSource = principal.token.claims["auth_source"]
    if (!acceptableSources.contains(authSource)) {
      throw AccessDeniedException("JWT token does not contain auth_source claim")
    }
    return principal.name to authSource as String
  }

  fun getJwt(): Jwt {
    val principal = SecurityContextHolder.getContext().authentication as AuthAwareAuthenticationToken
    return principal.token
  }
}
