package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier

import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config.RestClientRetry

interface TierClient {

  @GetExchange(value = "/v2/crn/{crn}/tier")
  fun getTier(@PathVariable crn: String): Tier

  @GetExchange(value = "/v3/crn/{crn}/tier")
  fun getTierV3(@PathVariable crn: String): Tier
}

@RestClientRetry
@Service
class TierCachingService(
  val tierClient: TierClient,
  @param:Value($$"${tier.v3-enabled:false}") val tierV3Enabled: Boolean,
) {
  @Cacheable(ApiCallKeys.GET_TIER, key = "#crn", sync = true)
  fun getTier(crn: String) = if (tierV3Enabled) tierClient.getTierV3(crn) else tierClient.getTier(crn)
}
