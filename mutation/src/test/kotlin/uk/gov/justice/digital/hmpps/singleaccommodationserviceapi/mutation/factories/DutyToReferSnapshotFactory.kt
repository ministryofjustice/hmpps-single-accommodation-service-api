package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.WithdrawalReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.DutyToReferAggregate.DutyToReferNote
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.DutyToReferAggregate.DutyToReferSnapshot
import java.time.LocalDate
import java.util.UUID

fun buildDutyToReferSnapshot(
  id: UUID = UUID.randomUUID(),
  caseId: UUID = UUID.randomUUID(),
  localAuthorityAreaId: UUID = UUID.randomUUID(),
  referenceNumber: String? = "DTR-REF-001",
  submissionDate: LocalDate = LocalDate.of(2026, 1, 15),
  status: DtrStatus = DtrStatus.SUBMITTED,
  notes: List<DutyToReferNote> = mutableListOf(buildDutyToReferNote()),
  withdrawalReason: WithdrawalReason? = null,
  withdrawalReasonOther: String? = null,
  outcomeReason: OutcomeReason? = null,
  submissionNote: String? = null,
  outcomeNote: String? = null,
) = DutyToReferSnapshot(
  id = id,
  caseId = caseId,
  localAuthorityAreaId = localAuthorityAreaId,
  referenceNumber = referenceNumber,
  submissionDate = submissionDate,
  status = status,
  notes = notes,
  withdrawalReason = withdrawalReason,
  withdrawalReasonOther = withdrawalReasonOther,
  outcomeReason = outcomeReason,
  submissionNote = submissionNote,
  outcomeNote = outcomeNote,
)

fun buildDutyToReferNote(id: UUID = UUID.randomUUID(), note: String = "Test Note") = DutyToReferNote(id, note)
