package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApprovedPremisesApiClient

@Service
class DummyService(
  private val approvedPremisesApiClient: ApprovedPremisesApiClient,
) {
  fun getPremisesSummary() = approvedPremisesApiClient.getPremises()
}
