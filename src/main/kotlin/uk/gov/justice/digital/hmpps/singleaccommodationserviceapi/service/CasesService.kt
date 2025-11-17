package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.model.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApDeliusContextApiClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ClientResult

@Service
class CasesService(
  private val apDeliusContextApiClient: ApDeliusContextApiClient,
) {

  fun getUserCases(crn: String): List<Case> {
    val result = apDeliusContextApiClient.getCaseSummaries(listOf(crn))

    val summaries = when (result) {
      is ClientResult.Success -> result.body.cases
      is ClientResult.Failure -> result.throwException()
    }

    return summaries
  }
}
