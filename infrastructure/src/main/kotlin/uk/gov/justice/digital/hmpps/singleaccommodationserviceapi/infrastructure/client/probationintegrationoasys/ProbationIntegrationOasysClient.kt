package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys

interface ProbationIntegrationOasysClient {
  @GetExchange(value = "/rosh/{crn}")
  fun getRoshDetails(@PathVariable crn: String): RoshDetails
}

@Service
open class ProbationIntegrationOasysCachingService(
  val probationIntegrationOasysClient: ProbationIntegrationOasysClient,
) {
  @Cacheable(ApiCallKeys.GET_ROSH_DETAIL, key = "#crn", sync = true)
  open fun getRoshDetails(crn: String) = probationIntegrationOasysClient.getRoshDetails(crn)
}
