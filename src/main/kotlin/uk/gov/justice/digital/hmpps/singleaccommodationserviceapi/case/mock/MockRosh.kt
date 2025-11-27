package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.Rosh
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RoshDetails
import java.time.Instant

class MockRosh {
  companion object {
    fun getMockedRoshDetails(): RoshDetails = RoshDetails(
      assessmentId = 12345,
      assessmentType = "assessmentType",
      dateCompleted = Instant.now(),
      assessorSignedDate = Instant.now(),
      initiationDate = Instant.now(),
      assessmentStatus = "assessmentStatus",
      superStatus = "superStatus",
      laterWIPAssessmentExists = true,
      limitedAccessOffender = false,
      lastUpdatedDate = Instant.now(),
      rosh = Rosh(
        riskChildrenCommunity = RiskLevel.VERY_HIGH,
        riskPrisonersCustody = RiskLevel.LOW,
        riskStaffCustody = RiskLevel.LOW,
        riskStaffCommunity = RiskLevel.LOW,
        riskKnownAdultCustody = RiskLevel.LOW,
        riskKnownAdultCommunity = RiskLevel.LOW,
        riskPublicCustody = RiskLevel.LOW,
        riskPublicCommunity = RiskLevel.LOW,
        riskChildrenCustody = RiskLevel.LOW,
      ),
    )
  }
}
