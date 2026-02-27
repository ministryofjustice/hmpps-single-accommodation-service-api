package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class DutyToReferDto(
  val crn: String,
  val serviceStatus: DtrServiceStatus,
  val action: RuleAction?,
  val submission: DtrSubmissionDto?,
)

data class DtrSubmissionDto(
  val id: UUID,
  val localAuthorityAreaId: UUID,
  val localAuthorityAreaName: String?,
  val referenceNumber: String?,
  val submissionDate: LocalDate,
  val outcomeStatus: DtrOutcomeStatus?,
  val outcomeDate: LocalDate?,
  val createdBy: String,
  @field:JsonFormat(
    shape = JsonFormat.Shape.STRING,
    pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
    timezone = "UTC",
  )
  val createdAt: Instant,
)

data class CreateDtrCommand(
  val localAuthorityAreaId: UUID,
  val submissionDate: LocalDate,
  val referenceNumber: String?,
)

enum class DtrServiceStatus {
  NOT_STARTED,
  UPCOMING,
  SUBMITTED,
  ACCEPTED,
  NOT_ACCEPTED,
  NOT_ELIGIBLE,
}

enum class DtrOutcomeStatus {
  ACCEPTED,
  NOT_ACCEPTED,
}
