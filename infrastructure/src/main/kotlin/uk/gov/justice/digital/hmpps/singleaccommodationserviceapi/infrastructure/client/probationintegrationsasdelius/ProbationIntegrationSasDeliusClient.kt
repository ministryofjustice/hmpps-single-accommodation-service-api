package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys

interface ProbationIntegrationSasDeliusClient {
  @PostExchange(value = "/case-list/summaries")
  fun getCaseList(@RequestBody username: String): CaseList
}

@Service
open class ProbationIntegrationSasDeliusCachingService(
  val probationIntegrationSasDeliusClient: ProbationIntegrationSasDeliusClient,
) {
  @Cacheable(ApiCallKeys.GET_CASE_LIST, key = "#username", sync = true)
  open fun getCaseList(username: String) = probationIntegrationSasDeliusClient.getCaseList(username)
}
