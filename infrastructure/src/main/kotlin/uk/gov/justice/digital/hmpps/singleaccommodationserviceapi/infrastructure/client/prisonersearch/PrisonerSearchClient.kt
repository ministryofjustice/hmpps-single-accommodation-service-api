package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch

import org.springframework.cache.annotation.Cacheable
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys

interface PrisonerSearchClient {
  @GetExchange(value = "/prisoner/{prisonNumber}")
  fun getPrisoner(@PathVariable prisonNumber: String): Prisoner
}

@Retryable(interceptor = "retryInterceptor")
@Service
class PrisonerSearchCachingService(
  private val prisonerSearchClient: PrisonerSearchClient,
) {
  @Cacheable(ApiCallKeys.GET_PRISONER, key = "#prisonNumber", sync = true)
  fun getPrisoner(prisonNumber: String) = prisonerSearchClient.getPrisoner(prisonNumber)
}
