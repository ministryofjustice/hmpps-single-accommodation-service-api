package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config.RestClientRetry

// this limits the data that's returned from the prisoner search API to the fields we are interested in
const val PRISONER_SEARCH_RESPONSE_FIELDS: String =
  "prisonerNumber,releaseDate,confirmedReleaseDate,inOutStatus,prisonId,prisonName,status"

interface PrisonerSearchClient {
  @GetExchange(value = "/prisoner/{prisonNumber}")
  fun getPrisoner(
    @PathVariable prisonNumber: String,
    @RequestParam responseFields: String = PRISONER_SEARCH_RESPONSE_FIELDS,
  ): Prisoner
}

@RestClientRetry
@Service
class PrisonerSearchCachingService(
  private val prisonerSearchClient: PrisonerSearchClient,
) {

  @Cacheable(ApiCallKeys.GET_PRISONER, key = "#prisonNumber", sync = true)
  fun getPrisoner(prisonNumber: String) = prisonerSearchClient.getPrisoner(prisonNumber)
}
