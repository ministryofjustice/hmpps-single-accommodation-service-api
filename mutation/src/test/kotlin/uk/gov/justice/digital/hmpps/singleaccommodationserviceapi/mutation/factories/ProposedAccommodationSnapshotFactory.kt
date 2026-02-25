package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate.ProposedAccommodationSnapshot
import java.time.LocalDate
import java.util.UUID

fun buildProposedAccommodationSnapshot(
  arrangementType: AccommodationArrangementType = AccommodationArrangementType.PRIVATE,
  arrangementSubType: AccommodationArrangementSubType? = AccommodationArrangementSubType.FRIENDS_OR_FAMILY,
  settledType: AccommodationSettledType = AccommodationSettledType.SETTLED,
  verificationStatus: VerificationStatus = VerificationStatus.NOT_CHECKED_YET,
  nextAccommodationStatus: NextAccommodationStatus = NextAccommodationStatus.TO_BE_DECIDED,
  offenderReleaseType: OffenderReleaseType? = OffenderReleaseType.REMAND,
) = ProposedAccommodationSnapshot(
  id = UUID.randomUUID(),
  crn = "X123456",
  name = "Test Accommodation",
  arrangementType = arrangementType,
  arrangementSubType = arrangementSubType,
  arrangementSubTypeDescription = "Description",
  settledType = settledType,
  verificationStatus = verificationStatus,
  nextAccommodationStatus = nextAccommodationStatus,
  offenderReleaseType = offenderReleaseType,
  startDate = LocalDate.now(),
  endDate = LocalDate.now().plusDays(7),
  address = buildAccommodationAddressDetails(),
)
