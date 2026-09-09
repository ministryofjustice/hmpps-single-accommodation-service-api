package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.OutOfRegionReferralAggregate.OutOfRegionReferralNote
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.OutOfRegionReferralAggregate.OutOfRegionReferralSnapshot
import java.time.LocalDate
import java.util.UUID

fun buildOutOfRegionReferralSnapshot(
  id: UUID = UUID.randomUUID(),
  caseId: UUID = UUID.randomUUID(),
  referenceNumber: String? = "OOR-REF-001",
  submissionDate: LocalDate = LocalDate.of(2026, 1, 15),
  status: OorStatus = OorStatus.SUBMITTED,
  notes: List<OutOfRegionReferralNote> = mutableListOf(buildOutOfRegionReferralNote()),
  outcomeReason: OutcomeReason? = null,
  submissionNote: String? = null,
  outcomeNote: String? = null,
) = OutOfRegionReferralSnapshot(
  id = id,
  caseId = caseId,
  referenceNumber = referenceNumber,
  submissionDate = submissionDate,
  status = status,
  notes = notes,
  outcomeReason = outcomeReason,
  submissionNote = submissionNote,
  outcomeNote = outcomeNote,
)

fun buildOutOfRegionReferralNote(id: UUID = UUID.randomUUID(), note: String = "Test Note") = OutOfRegionReferralNote(id, note)
