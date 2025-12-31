package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys

interface PrisonerSearchClient {
  @GetExchange(value = "/prisoner/{prisonerNumber}")
  fun getPrisoner(@PathVariable prisonerNumber: String): Prisoner
}

@Service
open class PrisonerSearchCachingService(
  private val prisonerSearchClient: PrisonerSearchClient,
) {
  @Cacheable(ApiCallKeys.GET_PRISONER, key = "#prisonerNumber")
  open fun getPrisoner(prisonerNumber: String) = prisonerSearchClient.getPrisoner(prisonerNumber)
}
