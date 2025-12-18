package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.prisonersearch

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_PRISONER

interface PrisonerSearchClient {
  @GetExchange(value = "/prisoner/{prisonerNumber}")
  fun getPrisoner(@PathVariable prisonerNumber: String): Prisoner
}

@Service
class PrisonerSearchCachingService(
  private val prisonerSearchClient: PrisonerSearchClient,
) {
  @Cacheable(GET_PRISONER, key = "#prisonerNumber")
  fun getPrisoner(prisonerNumber: String) = prisonerSearchClient.getPrisoner(prisonerNumber)
}
