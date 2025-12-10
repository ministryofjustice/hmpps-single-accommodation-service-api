package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RiskLevel

val mockPhotoUrl: String = "https://www.prison-officer-online-ac.co.uk/images/HMPPS-Logo-Black.svg "

fun getMockedCases(): List<CaseDto> {
  val caseDtos: MutableList<CaseDto> = mutableListOf()
  for (count in 1..10) {
    caseDtos += CaseDto(
      name = "Mock case $count",
      dateOfBirth = mockedLocalDate,
      crn = "CRN000$count",
      prisonNumber = "PRN000$count",
      tier = "TODO()",
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
