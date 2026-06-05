package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LocalAuthorityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.WithdrawalReason
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun buildDutyToReferDto(
  crn: String,
  id: UUID = UUID.randomUUID(),
  caseId: UUID = UUID.randomUUID(),
  localAuthorityAreaId: UUID = UUID.randomUUID(),
  referenceNumber: String? = "DTR-REF-001",
  submissionDate: LocalDate = LocalDate.of(2026, 1, 15),
  status: DtrStatus = DtrStatus.SUBMITTED,
  withdrawalReason: WithdrawalReason? = null,
  withdrawalReasonOther: String? = null,
  outcomeReason: OutcomeReason? = null,
  createdAt: Instant = Instant.now(),
) = DutyToReferDto(
  caseId,
  crn,
  status,
  submission = DtrSubmissionDto(
    id = id,
    localAuthority = LocalAuthorityDto(localAuthorityAreaId, "Test Local Authority"),
    referenceNumber = referenceNumber,
    submissionDate = submissionDate,
    createdBy = "Test User",
    createdAt = createdAt,
    withdrawalReason = withdrawalReason,
    withdrawalReasonOther = withdrawalReasonOther,
    outcomeReason = outcomeReason,
  ),
)
