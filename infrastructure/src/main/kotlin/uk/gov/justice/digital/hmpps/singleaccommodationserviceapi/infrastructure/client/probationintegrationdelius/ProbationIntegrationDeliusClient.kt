package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.PostExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys

interface ProbationIntegrationDeliusClient {
  @PostExchange(value = "/probation-cases/summaries")
  fun postCaseSummaries(@RequestBody crns: List<String>): CaseSummaries

  @GetExchange(value = "/staff/{username}")
  fun getStaffDetail(@PathVariable username: String): StaffDetail?
}

@Service
open class ProbationIntegrationDeliusCachingService(
  val probationIntegrationDeliusClient: ProbationIntegrationDeliusClient,
) {
  @Cacheable(ApiCallKeys.GET_CASE_SUMMARY, key = "#crn", sync = true)
  open fun getCaseSummary(crn: String) = probationIntegrationDeliusClient.postCaseSummaries(listOf(crn))

  open fun getCaseSummaries(crns: List<String>) = probationIntegrationDeliusClient.postCaseSummaries(crns)

  @Cacheable(ApiCallKeys.GET_STAFF_DETAIL, key = "#username", sync = true)
  open fun getStaffDetail(username: String) = probationIntegrationDeliusClient.getStaffDetail(username)
}
