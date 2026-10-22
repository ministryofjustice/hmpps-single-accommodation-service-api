package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.nomisuserroles

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_NOMIS_USER_DETAIL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config.WebClientRetry
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.Username

interface NomisUserRolesClient {

  @WebClientRetry
  @GetExchange(value = "/username/{username}")
  fun getUserDetails(username: String): NomisUserDetail
}

@Service
class NomisUserRolesService(
  private val nomisUserRolesClient: NomisUserRolesClient,
) {

  @Cacheable(key = GET_NOMIS_USER_DETAIL, sync = true)
  fun getNomisUserDetail(username: Username): NomisUserDetail = nomisUserRolesClient.getUserDetails(username.value)
}
