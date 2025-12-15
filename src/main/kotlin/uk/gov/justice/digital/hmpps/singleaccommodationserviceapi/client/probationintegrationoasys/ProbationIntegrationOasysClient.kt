package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_ROSH_DETAIL

interface ProbationIntegrationOasysClient {
  @GetExchange(value = "/rosh/{crn}")
  fun getRoshDetails(@PathVariable crn: String): RoshDetails
}

@Service
class ProbationIntegrationOasysCachingService(
  val probationIntegrationOasysClient: ProbationIntegrationOasysClient,
) {
  @Cacheable(GET_ROSH_DETAIL, key = "#crn", sync = true)
  fun getRoshDetails(crn: String) = probationIntegrationOasysClient.getRoshDetails(crn)
}
