package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.nomisuserroles

import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config.DefaultRetry

interface NomisUserRolesClient {
  @GetExchange(value = "/me")
  fun getUserDetailsForMe(@RequestHeader(value = "Authorization", required = false) authorization: String?): NomisUserDetail?
}

@DefaultRetry
@Service
class NomisUserRolesService(
  private val nomisUserRolesClient: NomisUserRolesClient,
) {
  fun getUserDetailsForMe(jwt: Jwt) = nomisUserRolesClient.getUserDetailsForMe("Bearer ${jwt.tokenValue}")
}
