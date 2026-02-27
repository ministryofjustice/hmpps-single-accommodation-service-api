package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.nomisuserroles

import org.springframework.stereotype.Service
import org.springframework.web.service.annotation.GetExchange

interface NomisUserRolesClient {
  @GetExchange(value = "/me")
  fun getUserDetailsForMe(): NomisUserDetail?
}

@Service
open class NomisUserRolesService(
  private val nomisUserRolesClient: NomisUserRolesClient,
) {
  open fun getUserDetailsForMe() = nomisUserRolesClient.getUserDetailsForMe()
}
