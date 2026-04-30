package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys

interface SasAndDeliusClient {
  @GetExchange(value = "/case-list/{username}")
  fun getCaseList(@PathVariable username: String): CaseList

  @GetExchange(value = "/case/{username}/{crn}")
  fun getCase(@PathVariable username: String, @PathVariable crn: String): Case
}

@Service
class SasAndDeliusCachingService(
  val sasAndDeliusClient: SasAndDeliusClient,
) {
  @Cacheable(ApiCallKeys.GET_CASE_LIST, key = "#username", sync = true)
  fun getCaseList(username: String) = sasAndDeliusClient.getCaseList(username)

  @Cacheable(ApiCallKeys.GET_CASE, key = "#username", sync = true)
  fun getCase(username: String, crn: String) = sasAndDeliusClient.getCase(username, crn)
}
