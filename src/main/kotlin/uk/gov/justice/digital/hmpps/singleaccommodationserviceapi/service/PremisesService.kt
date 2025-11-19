package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.model.Cas3PremisesSearchResults
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApprovedPremisesApiClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ClientResult
import java.util.UUID

@Service
class PremisesService(
  private val approvedPremisesApiClient: ApprovedPremisesApiClient,
) {

  fun searchPremises(postcode: String, probationRegionId: UUID): Cas3PremisesSearchResults {
    val result = approvedPremisesApiClient.getPremises(postcode, probationRegionId)
    return when (result) {
      is ClientResult.Success -> result.body
      is ClientResult.Failure -> result.throwException()
    }
  }
}
