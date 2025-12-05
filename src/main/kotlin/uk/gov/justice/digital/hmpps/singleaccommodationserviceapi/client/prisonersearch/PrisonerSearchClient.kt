package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.prisonersearch

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange

interface PrisonerSearchClient {
  @GetExchange(value = "/prisoner/{prisonerNumber}")
  fun getPrisoner(@PathVariable prisonerNumber: String): Prisoner
}

@Service
class PrisonerSearchCachingService(
  private val prisonerSearchClient: PrisonerSearchClient,
) {
  @Cacheable("getPrisonerByPrisonerNumber", key = "#prisonerNumber")
  fun getPrisoner(prisonerNumber: String) = prisonerSearchClient.getPrisoner(prisonerNumber)
}
