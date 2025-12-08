package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.AccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.AccommodationType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.CurrentAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.NextAccommodationDto
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
  accommodationStatus: AccommodationStatus = AccommodationStatus(
    CurrentAccommodationDto(type = AccommodationType.CAS1_MOCK, endDate = LocalDate.now().plusDays(5)),
    NextAccommodationDto(type = AccommodationType.PRIVATE_ADDRESS_MOCK, startDate = LocalDate.now().plusDays(10)),
  ),
  photoUrl: String? = "!!https://www.replace-this-with-a-real-url.com",
) = CaseDto(
  name = name,
  dateOfBirth = dateOfBirth,
  crn = crn,
  prisonNumber = prisonNumber,
  tier = tier,
  riskLevel = riskLevel,
  pncReference = pncReference,
  assignedTo = assignedTo,
  currentAccommodation = accommodationStatus.currentAccommodation,
  nextAccommodation = accommodationStatus.nextAccommodation,
  photoUrl = photoUrl,
)
