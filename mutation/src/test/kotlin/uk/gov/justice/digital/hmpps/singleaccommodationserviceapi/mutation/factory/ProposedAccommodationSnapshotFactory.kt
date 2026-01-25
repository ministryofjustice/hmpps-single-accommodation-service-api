package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate.ProposedAccommodationSnapshot
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun buildSnapshot(
  arrangementType: AccommodationArrangementType = AccommodationArrangementType.PRIVATE,
  arrangementSubType: AccommodationArrangementSubType? = AccommodationArrangementSubType.FRIENDS_OR_FAMILY,
  settledType: AccommodationSettledType = AccommodationSettledType.SETTLED,
  status: AccommodationStatus = AccommodationStatus.NOT_CHECKED_YET,
  offenderReleaseType: OffenderReleaseType? = OffenderReleaseType.REMAND,
) = ProposedAccommodationSnapshot(
  id = UUID.randomUUID(),
  crn = "X123456",
  name = "Test Accommodation",
  arrangementType = arrangementType,
  arrangementSubType = arrangementSubType,
  arrangementSubTypeDescription = "Description",
  settledType = settledType,
  status = status,
  offenderReleaseType = offenderReleaseType,
  startDate = LocalDate.now(),
  endDate = LocalDate.now().plusDays(7),
  address = buildAccommodationAddressDetails(),
  createdAt = Instant.now(),
  lastUpdatedAt = Instant.now(),
)
