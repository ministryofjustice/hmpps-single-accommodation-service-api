package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.PostExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys

interface ProbationIntegrationOasysClient {
  @GetExchange(value = "/rosh/{crn}")
  fun getRoshDetail(@PathVariable crn: String): RoshDetails

  @PostExchange(value = "/rosh/bulk")
  fun postRoshDetails(@RequestBody crns: List<String>): List<RoshDetails>
}

@Service
open class ProbationIntegrationOasysCachingService(
  val probationIntegrationOasysClient: ProbationIntegrationOasysClient,
) {
  @Cacheable(ApiCallKeys.GET_ROSH_DETAIL, key = "#crn", sync = true)
  open fun getRoshDetail(crn: String) = probationIntegrationOasysClient.getRoshDetail(crn)

  @Cacheable(ApiCallKeys.GET_ROSH_DETAILS, key = "#crns", sync = true)
  open fun getRoshDetails(crns: List<String>) = probationIntegrationOasysClient.postRoshDetails(crns)
}
