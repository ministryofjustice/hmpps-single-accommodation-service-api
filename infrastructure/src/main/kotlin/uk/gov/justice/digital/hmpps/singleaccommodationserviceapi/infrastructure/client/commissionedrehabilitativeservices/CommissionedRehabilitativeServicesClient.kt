package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.getOrNullWhenNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config.RestClientRetry

interface CommissionedRehabilitativeServicesClient {

  @GetExchange(value = "/sas-referral-details/{crn}")
  fun getCrs(@PathVariable crn: String): List<CommissionedRehabilitativeServices>
}

@RestClientRetry
@Service
class CommissionedRehabilitativeServicesCachingService(
  val commissionedRehabilitativeServicesClient: CommissionedRehabilitativeServicesClient,
) {
  @Cacheable(ApiCallKeys.GET_CRS, sync = true)
  fun getCrs(crn: String) = getOrNullWhenNotFound {
    commissionedRehabilitativeServicesClient.getCrs(crn)
  }
}
