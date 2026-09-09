package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutOfRegionReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutcomeReason
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun buildOutOfRegionReferralDto(
  crn: String = "CR12345N",
  caseId: UUID = UUID.randomUUID(),
  status: OorStatus = OorStatus.NOT_ACCEPTED,
  submissionDate: LocalDate = LocalDate.now(),
  submission: OorSubmissionDto? = buildOorSubmission(submissionDate = submissionDate),
) = OutOfRegionReferralDto(
  crn = crn,
  caseId = caseId,
  status = status,
  submission = submission,
)

fun buildOorSubmission(
  id: UUID = UUID.randomUUID(),
  referenceNumber: String = "REFERENCE-123",
  submissionDate: LocalDate = LocalDate.now(),
  createdBy: String = "Someone",
  createdByUsername: String? = null,
  createdAt: Instant = Instant.now(),
) = OorSubmissionDto(
  id = id,
  referenceNumber = referenceNumber,
  submissionDate = submissionDate,
  createdBy = createdBy,
  createdByUsername = createdByUsername,
  createdAt = createdAt,
  outcomeReason = OutcomeReason.NO_LOCAL_CONNECTION,
)
