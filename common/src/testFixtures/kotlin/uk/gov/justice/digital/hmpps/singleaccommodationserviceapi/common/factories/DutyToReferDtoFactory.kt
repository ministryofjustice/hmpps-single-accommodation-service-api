package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LocalAuthorityDto
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun buildDutyToReferDto(
  crn: String = "CR12345N",
  caseId: UUID = UUID.randomUUID(),
  status: DtrStatus = DtrStatus.SUBMITTED,
  submission: DtrSubmissionDto? = buildDtrSubmission(),
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
  createdAt: Instant = Instant.now(),
) = DtrSubmissionDto(
  id = id,
  localAuthority = localAuthority,
  referenceNumber = referenceNumber,
  submissionDate = submissionDate,
  createdBy = createdBy,
  createdAt = createdAt,
)

fun buildLocalAuthorityDto(
  localAuthorityAreaId: UUID = UUID.randomUUID(),
  localAuthorityAreaName: String = "localAuthorityAreaName",
) = LocalAuthorityDto(
  localAuthorityAreaId = localAuthorityAreaId,
  localAuthorityAreaName = localAuthorityAreaName,
)
