package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.OtherAccommodationReferralAggregate.OtherAccommodationReferralNote
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.OtherAccommodationReferralAggregate.OtherAccommodationReferralSnapshot
import java.time.LocalDate
import java.util.UUID

fun buildOtherAccommodationReferralSnapshot(
  id: UUID = UUID.randomUUID(),
  caseId: UUID = UUID.randomUUID(),
  crn: String = "X123456",
  localAuthorityAreaId: UUID = UUID.randomUUID(),
  referenceNumber: String? = "REF-001",
  submissionDate: LocalDate = LocalDate.of(2026, 2, 20),
  status: OtherAccommodationReferralStatus = OtherAccommodationReferralStatus.SUBMITTED,
  organisationName: String? = "Organisation name",
  website: String? = "https://www.charity.org",
  submissionNote: String? = "A submission note",
  notes: List<OtherAccommodationReferralNote> = emptyList(),
) = OtherAccommodationReferralSnapshot(
  id = id,
  caseId = caseId,
  crn = crn,
  localAuthorityAreaId = localAuthorityAreaId,
  referenceNumber = referenceNumber,
  submissionDate = submissionDate,
  status = status,
  organisationName = organisationName,
  website = website,
  submissionNote = submissionNote,
  notes = notes,
)

fun buildOtherAccommodationReferralNote(id: UUID = UUID.randomUUID(), note: String = "Test note") = OtherAccommodationReferralNote(id, note)
