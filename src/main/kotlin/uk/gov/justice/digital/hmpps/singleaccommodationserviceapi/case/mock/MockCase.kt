package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.mock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RiskLevel
import java.time.LocalDate

fun getMockedCases(): List<CaseDto> {
  val caseDtos: MutableList<CaseDto> = mutableListOf()
  for (count in 1..10) {
    caseDtos += CaseDto(
      name = "Mock case $count",
      dateOfBirth = LocalDate.now().minusYears(count.toLong()),
      crn = "CRN000$count",
      prisonNumber = "PRN000$count",
      tier = "TODO()",
      riskLevel = RiskLevel.VERY_HIGH,
      pncReference = "TODO()",
      assignedTo = null,
      currentAccommodation = null,
      nextAccommodation = null,
    )
  }
  return caseDtos
}
