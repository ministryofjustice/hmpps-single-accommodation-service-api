package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate.ProposedAccommodationNoteSnapshot
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate.ProposedAccommodationSnapshot
import java.time.LocalDate
import java.util.UUID

fun buildProposedAccommodationSnapshot(
  cprAddressId: UUID? = null,
  accommodationSource: AccommodationSource = AccommodationSource.SAS,
  accommodationType: AccommodationTypeDto = buildAccommodationTypeDto(
    code = "A07B",
    description = "Friends/Family (settled)",
  ),
  accommodationStatus: AccommodationStatusDto? = null,
  verificationStatus: VerificationStatus = VerificationStatus.NOT_CHECKED_YET,
  nextAccommodationStatus: NextAccommodationStatus = NextAccommodationStatus.TO_BE_DECIDED,
  typeVerified: Boolean = false,
  noFixedAbode: Boolean = false,
  notes: List<ProposedAccommodationNoteSnapshot> = mutableListOf(buildNote()),
) = ProposedAccommodationSnapshot(
  id = UUID.randomUUID(),
  caseId = UUID.randomUUID(),
  cprAddressId = cprAddressId,
  accommodationSource = accommodationSource,
  name = "Test Accommodation",
  accommodationType = accommodationType,
  accommodationStatus = accommodationStatus,
  verificationStatus = verificationStatus,
  nextAccommodationStatus = nextAccommodationStatus,
  startDate = LocalDate.now(),
  endDate = LocalDate.now().plusDays(7),
  typeVerified = typeVerified,
  noFixedAbode = noFixedAbode,
  address = buildAccommodationAddressDetails(),
  notes = notes,
)

fun buildNote(id: UUID = UUID.randomUUID(), note: String = "Test Note") = ProposedAccommodationNoteSnapshot(id, note)
