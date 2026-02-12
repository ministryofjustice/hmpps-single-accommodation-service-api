package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import java.net.URI

interface TierClient {

  @GetExchange(value = "/crn/{crn}/tier")
  fun getTier(@PathVariable crn: String): Tier

  @GetExchange
  fun fetchTier(uri: URI): Tier
}

@Service
open class TierCachingService(
  val tierClient: TierClient,
) {
  @Cacheable(ApiCallKeys.GET_TIER, key = "#crn", sync = true)
  open fun getTier(crn: String) = tierClient.getTier(crn)
}
