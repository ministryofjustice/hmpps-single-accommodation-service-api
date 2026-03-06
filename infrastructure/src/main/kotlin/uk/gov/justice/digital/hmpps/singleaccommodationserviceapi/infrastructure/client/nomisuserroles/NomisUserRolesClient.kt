package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.nomisuserroles

import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.service.annotation.GetExchange

interface NomisUserRolesClient {
  @GetExchange(value = "/me")
  fun getUserDetailsForMe(): NomisUserDetail?

  @GetExchange(value = "/me")
  fun getUserDetailsForMe(@RequestHeader(required = false) bearer: String): NomisUserDetail?
}

@Service
class NomisUserRolesService(
  private val nomisUserRolesClient: NomisUserRolesClient,
) {
  fun getUserDetailsForMe(jwt: Jwt) = nomisUserRolesClient.getUserDetailsForMe("Bearer ${jwt.tokenValue}")
}
