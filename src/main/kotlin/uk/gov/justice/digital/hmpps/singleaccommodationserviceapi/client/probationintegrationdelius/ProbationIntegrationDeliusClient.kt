package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CASE_SUMMARY

interface ProbationIntegrationDeliusClient {
  @PostExchange(value = "/probation-cases/summaries")
  fun postCaseSummaries(@RequestBody crns: List<String>): CaseSummaries
}

@Service
class ProbationIntegrationDeliusCachingService(
  val probationIntegrationDeliusClient: ProbationIntegrationDeliusClient,
) {
  @Cacheable(GET_CASE_SUMMARY, key = "#crn", sync = true)
  fun getCaseSummary(crn: String) = probationIntegrationDeliusClient.postCaseSummaries(listOf(crn))

  fun getCaseSummaries(crns: List<String>) = probationIntegrationDeliusClient.postCaseSummaries(crns)
}
