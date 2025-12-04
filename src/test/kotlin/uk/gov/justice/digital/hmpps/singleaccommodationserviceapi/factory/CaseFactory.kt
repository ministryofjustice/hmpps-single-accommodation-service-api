package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.CurrentAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.NextAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RiskLevel
import java.time.LocalDate

fun buildCaseDto(
  name: String = "Case Name",
  dateOfBirth: LocalDate = LocalDate.of(2000, 12, 3),
  crn: String = "CR12345N",
  prisonNumber: String = "PR98765N",
  tier: String = "Tier1",
  riskLevel: RiskLevel = RiskLevel.MEDIUM,
  pncReference: String = "pncReference",
  assignedTo: AssignedToDto = AssignedToDto(id = 123456, name = "Assigned To"),
  currentAccommodation: CurrentAccommodationDto = CurrentAccommodationDto(
    type = "Type1",
    endDate = LocalDate.now().plusDays(5),
  ),
  nextAccommodation: NextAccommodationDto = NextAccommodationDto(
    type = "Type2",
    startDate = LocalDate.now().plusDays(10),
  ),
) = CaseDto(
  name = name,
  dateOfBirth = dateOfBirth,
  crn = crn,
  prisonNumber = prisonNumber,
  tier = tier,
  riskLevel = riskLevel,
  pncReference = pncReference,
  assignedTo = assignedTo,
  currentAccommodation = currentAccommodation,
  nextAccommodation = nextAccommodation,
)
