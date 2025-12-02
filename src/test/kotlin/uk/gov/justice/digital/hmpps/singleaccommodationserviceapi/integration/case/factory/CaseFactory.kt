package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.AssignedTo
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.CurrentAccommodation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.NextAccommodation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RiskLevel
import java.time.LocalDate

fun buildCaseDto(
  name: String = "Case Name",
  dateOfBirth: LocalDate = LocalDate.now().minusYears(20),
  crn: String = "CR12345N",
  prisonNumber: String = "PR98765N",
  tier: String = "Tier1",
  riskLevel: RiskLevel = RiskLevel.MEDIUM,
  pncReference: String = "pncReference",
  assignedTo: AssignedTo = AssignedTo(id = 123456, name = "Assigned To"),
  currentAccommodation: CurrentAccommodation = CurrentAccommodation(
    type = "Type1",
    endDate = LocalDate.now().plusDays(5),
  ),
  nextAccommodation: NextAccommodation = NextAccommodation(
    type = "Type2",
    startDate = LocalDate.now().plusDays(10),
  ),
) = Case(
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
