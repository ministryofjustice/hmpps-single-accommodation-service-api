package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.PostExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config.WebClientRetry

interface ApprovedPremisesAndDeliusClient {
  // this post request is safe to retry as it does not create data
  @WebClientRetry
  @PostExchange(value = "/probation-cases/summaries")
  fun postCaseSummaries(@RequestBody crns: List<String>): CaseSummaries

  @WebClientRetry
  @GetExchange(value = "/staff/{username}")
  fun getStaffDetail(@PathVariable username: String): StaffDetail?
}

@Service
class ApprovedPremisesAndDeliusCachingService(
  val approvedPremisesAndDeliusClient: ApprovedPremisesAndDeliusClient,
) {

  @Cacheable(ApiCallKeys.GET_STAFF_DETAIL, key = "#username", sync = true)
  fun getStaffDetail(username: String) = approvedPremisesAndDeliusClient.getStaffDetail(username)

  fun postCaseSummaries(crns: List<String>) = approvedPremisesAndDeliusClient.postCaseSummaries(crns)
}
