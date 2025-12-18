package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.TierScore

val mockPhotoUrl: String =
  "https://github.com/ministryofjustice/hmpps-single-accommodation-service-prototype/blob/main/app/assets/images/profile-placeholder-2.jpg"
fun getMockedCases(): List<CaseDto> {
  val caseDtos: MutableList<CaseDto> = mutableListOf()
  for (count in 1..10) {
    caseDtos += CaseDto(
      name = "Mock case $count",
      dateOfBirth = mockedLocalDate,
      crn = "CRN000$count",
      prisonNumber = "PRN000$count",
      tier = TierScore.A3,
      riskLevel = RiskLevel.VERY_HIGH,
      pncReference = "TODO()",
      assignedTo = null,
      currentAccommodation = null,
      nextAccommodation = null,
      photoUrl = "TODO()",
    )
  }
  return caseDtos
}
