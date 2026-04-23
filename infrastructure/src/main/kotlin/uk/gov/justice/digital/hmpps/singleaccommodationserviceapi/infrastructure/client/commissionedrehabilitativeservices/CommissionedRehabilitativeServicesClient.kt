package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys

interface CommissionedRehabilitativeServicesClient {

  @GetExchange(value = "/crs/{crn}")
  fun getCrs(@PathVariable crn: String): CommissionedRehabilitativeServices
}

@Service
open class CommissionedRehabilitativeServicesCachingService(
  val commissionedRehabilitativeServicesClient: CommissionedRehabilitativeServicesClient,
) {
  @Cacheable(ApiCallKeys.GET_CRS, key = "#crn", sync = true)
  open fun getCrs(crn: String) = commissionedRehabilitativeServicesClient.getCrs(crn)
}
