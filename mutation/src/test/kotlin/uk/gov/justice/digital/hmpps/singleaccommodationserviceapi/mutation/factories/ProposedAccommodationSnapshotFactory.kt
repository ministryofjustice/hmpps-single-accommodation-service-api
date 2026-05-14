package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate.ProposedAccommodationNoteSnapshot
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate.ProposedAccommodationSnapshot
import java.time.LocalDate
import java.util.UUID

fun buildProposedAccommodationSnapshot(
  accommodationType: AccommodationTypeDto = buildAccommodationTypeDto(
    code = "A07B",
    description = "Friends/Family (settled)",
  ),
  accommodationStatus: AccommodationStatusDto? = null,
  verificationStatus: VerificationStatus = VerificationStatus.NOT_CHECKED_YET,
  nextAccommodationStatus: NextAccommodationStatus = NextAccommodationStatus.TO_BE_DECIDED,
  notes: List<ProposedAccommodationNoteSnapshot> = mutableListOf(buildNote()),
) = ProposedAccommodationSnapshot(
  id = UUID.randomUUID(),
  caseId = UUID.randomUUID(),
  name = "Test Accommodation",
  accommodationType = accommodationType,
  accommodationStatus = accommodationStatus,
  verificationStatus = verificationStatus,
  nextAccommodationStatus = nextAccommodationStatus,
  startDate = LocalDate.now(),
  endDate = LocalDate.now().plusDays(7),
  address = buildAccommodationAddressDetails(),
  notes = notes,
)

fun buildNote(id: UUID = UUID.randomUUID(), note: String = "Test Note") = ProposedAccommodationNoteSnapshot(id, note)
