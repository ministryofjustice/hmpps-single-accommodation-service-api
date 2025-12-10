package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.Rosh
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RoshDetails

fun getMockedRoshDetails() = RoshDetails(
  assessmentId = 12345,
  assessmentType = "assessmentType",
  dateCompleted = mockedZonedDateTime,
  assessorSignedDate = mockedZonedDateTime,
  initiationDate = mockedZonedDateTime,
  assessmentStatus = "assessmentStatus",
  superStatus = "superStatus",
  laterWIPAssessmentExists = true,
  limitedAccessOffender = false,
  lastUpdatedDate = mockedZonedDateTime,
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
