package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange

interface TierClient {

  @GetExchange(value = "/crn/{crn}/tier")
  fun getTier(@PathVariable crn: String): Tier
}

@Service
class TierCachingService(
  val tierClient: TierClient,
) {
  @Cacheable("getTierByCrn", key = "#crn", sync = true)
  fun getTier(crn: String) = tierClient.getTier(crn)
}
