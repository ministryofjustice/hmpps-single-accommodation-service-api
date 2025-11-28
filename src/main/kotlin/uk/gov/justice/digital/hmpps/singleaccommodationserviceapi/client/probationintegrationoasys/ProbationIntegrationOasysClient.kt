package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange

interface ProbationIntegrationOasysClient {
  @GetExchange(value = "/rosh-summary/{crn}")
  fun getRoshSummary(@PathVariable crn: String): RoshSummary
}

@Service
class ProbationIntegrationOasysCachingService(
  val probationIntegrationOasysClient: ProbationIntegrationOasysClient,
) {
  @Cacheable("getRoshSummaryByCrn", key = "#crn")
  fun getRoshSummary(crn: String) = probationIntegrationOasysClient.getRoshSummary(crn)
}
