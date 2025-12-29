package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.Rosh
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.TestData
import java.time.ZonedDateTime

@TestData
fun buildRoshDetails(
  assessmentId: Long = 12345,
  assessmentType: String = "assessmentType",
  dateCompleted: ZonedDateTime = ZonedDateTime.now(),
  assessorSignedDate: ZonedDateTime = ZonedDateTime.now(),
  initiationDate: ZonedDateTime = ZonedDateTime.now(),
  assessmentStatus: String = "assessmentStatus",
  superStatus: String = "superStatus",
  laterWIPAssessmentExists: Boolean = true,
  limitedAccessOffender: Boolean = false,
  lastUpdatedDate: ZonedDateTime = ZonedDateTime.now(),
  rosh: Rosh = buildRosh(),
) = RoshDetails(
  assessmentId = assessmentId,
  assessmentType = assessmentType,
  dateCompleted = dateCompleted,
  assessorSignedDate = assessorSignedDate,
  initiationDate = initiationDate,
  assessmentStatus = assessmentStatus,
  superStatus = superStatus,
  laterWIPAssessmentExists = laterWIPAssessmentExists,
  limitedAccessOffender = limitedAccessOffender,
  lastUpdatedDate = lastUpdatedDate,
  rosh = rosh,
)

@TestData
fun buildRosh(
  riskChildrenCommunity: RiskLevel = RiskLevel.VERY_HIGH,
  riskPrisonersCustody: RiskLevel = RiskLevel.LOW,
  riskStaffCustody: RiskLevel = RiskLevel.LOW,
  riskStaffCommunity: RiskLevel = RiskLevel.LOW,
  riskKnownAdultCustody: RiskLevel = RiskLevel.LOW,
  riskKnownAdultCommunity: RiskLevel = RiskLevel.LOW,
  riskPublicCustody: RiskLevel = RiskLevel.LOW,
  riskPublicCommunity: RiskLevel = RiskLevel.LOW,
  riskChildrenCustody: RiskLevel = RiskLevel.LOW,
) = Rosh(
  riskChildrenCommunity = riskChildrenCommunity,
  riskPrisonersCustody = riskPrisonersCustody,
  riskStaffCustody = riskStaffCustody,
  riskStaffCommunity = riskStaffCommunity,
  riskKnownAdultCustody = riskKnownAdultCustody,
  riskKnownAdultCommunity = riskKnownAdultCommunity,
  riskPublicCustody = riskPublicCustody,
  riskPublicCommunity = riskPublicCommunity,
  riskChildrenCustody = riskChildrenCustody,
)
