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
  crn: String = "CR12345N",
  caseId: UUID = UUID.randomUUID(),
  status: DtrStatus = DtrStatus.NOT_ACCEPTED,
  submissionDate: LocalDate = LocalDate.now(),
  submission: DtrSubmissionDto? = buildDtrSubmission(submissionDate = submissionDate),
) = DutyToReferDto(
  crn = crn,
  caseId = caseId,
  status = status,
  submission = submission,
)

fun buildDtrSubmission(
  id: UUID = UUID.randomUUID(),
  localAuthority: LocalAuthorityDto = buildLocalAuthorityDto(),
  referenceNumber: String = "REFERENCE-123",
  submissionDate: LocalDate = LocalDate.now(),
  createdBy: String = "Someone",
  createdByUsername: String? = null,
  createdAt: Instant = Instant.now(),
) = DtrSubmissionDto(
  id = id,
  localAuthority = localAuthority,
  referenceNumber = referenceNumber,
  submissionDate = submissionDate,
  createdBy = createdBy,
  createdByUsername = createdByUsername,
  createdAt = createdAt,
  withdrawalReason = WithdrawalReason.NOT_ELIGIBLE,
  withdrawalReasonOther = "Some other reason",
  outcomeReason = OutcomeReason.NO_LOCAL_CONNECTION,
)

fun buildLocalAuthorityDto(
  localAuthorityAreaId: UUID = UUID.randomUUID(),
  localAuthorityAreaName: String = "localAuthorityAreaName",
) = LocalAuthorityDto(
  localAuthorityAreaId = localAuthorityAreaId,
  localAuthorityAreaName = localAuthorityAreaName,
)
