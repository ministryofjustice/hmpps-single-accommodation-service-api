package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.PostExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import java.net.URI

interface TierClient {

  @GetExchange(value = "/crn/{crn}/tier")
  fun getTier(@PathVariable crn: String): Tier

  @GetExchange
  fun getTier(uri: URI): Tier

  @PostExchange(value = "/crn/bulk/tier")
  fun postTiers(@RequestBody crns: List<String>): List<Tier>
}

@Service
open class TierCachingService(
  val tierClient: TierClient,
) {
  @Cacheable(ApiCallKeys.GET_TIER, key = "#crn", sync = true)
  open fun getTier(crn: String) = tierClient.getTier(crn)

  @Cacheable(ApiCallKeys.GET_TIERS, key = "#crns", sync = true)
  open fun getTiers(crns: List<String>) = tierClient.postTiers(crns)
}
