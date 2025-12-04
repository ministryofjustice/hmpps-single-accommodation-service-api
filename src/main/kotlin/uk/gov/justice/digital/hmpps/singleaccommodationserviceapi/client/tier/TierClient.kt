package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_TIER

interface TierClient {

  @GetExchange(value = "/crn/{crn}/tier")
  fun getTier(@PathVariable crn: String): Tier
}

@Service
class TierCachingService(
  val tierClient: TierClient,
) {
  @Cacheable(GET_TIER, key = "#crn", sync = true)
  fun getTier(crn: String) = tierClient.getTier(crn)
}
