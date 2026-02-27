package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import java.util.UUID

class AuthAwareAuthenticationToken(
  jwt: Jwt,
  private val aPrincipal: UserPrincipal,
  authorities: Collection<GrantedAuthority>,
) : JwtAuthenticationToken(jwt, authorities) {
  override fun getPrincipal(): UserPrincipal = aPrincipal
}

data class UserPrincipal(
  val userUuid: UUID,
  val username: String,
  val authSource: AuthSource,
)