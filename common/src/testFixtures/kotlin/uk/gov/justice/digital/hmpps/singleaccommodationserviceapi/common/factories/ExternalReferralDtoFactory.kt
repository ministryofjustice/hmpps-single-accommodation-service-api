package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralWithdrawalReason
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun buildExternalReferralDto(
  caseId: UUID = UUID.randomUUID(),
  crn: String = "X123456",
  status: ExternalReferralStatus = ExternalReferralStatus.SUBMITTED,
  submission: ExternalReferralSubmissionDto = buildExternalReferralSubmissionDto(),
) = ExternalReferralDto(
  caseId = caseId,
  crn = crn,
  status = status,
  submission = submission,
)

fun buildExternalReferralSubmissionDto(
  id: UUID = UUID.randomUUID(),
  referenceNumber: String? = "OA-REF-001",
  submissionDate: LocalDate = LocalDate.now(),
  createdBy: String = "Someone",
  createdByUsername: String = "SOMEONE",
  createdAt: Instant = Instant.now(),
  organisationName: String? = "Organisation name",
  website: String? = null,
  submissionNote: String? = null,
  email: String? = null,
  phoneNumber: String? = null,
  withdrawalReason: ExternalReferralWithdrawalReason? = null,
  withdrawalNote: String? = null,
) = ExternalReferralSubmissionDto(
  id = id,
  referenceNumber = referenceNumber,
  submissionDate = submissionDate,
  createdBy = createdBy,
  createdByUsername = createdByUsername,
  createdAt = createdAt,
  organisationName = organisationName,
  website = website,
  submissionNote = submissionNote,
  email = email,
  phoneNumber = phoneNumber,
  withdrawalReason = withdrawalReason,
  withdrawalNote = withdrawalNote,
)
