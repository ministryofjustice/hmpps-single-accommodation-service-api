package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config.WebClientRetry

interface TierClient {
  @WebClientRetry
  @GetExchange(value = "/v3/crn/{crn}/tier")
  fun getTierV3(@PathVariable crn: String): Tier
}

@Service
class TierCachingService(
  val tierClient: TierClient,
) {
  @Cacheable(ApiCallKeys.GET_TIER_V3, key = "#crn", sync = true)
  fun getTier(crn: String) = tierClient.getTierV3(crn)
}
