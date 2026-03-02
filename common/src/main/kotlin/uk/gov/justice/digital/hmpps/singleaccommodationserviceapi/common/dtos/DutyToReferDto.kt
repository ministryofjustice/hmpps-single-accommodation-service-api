package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class DutyToReferDto(
  val crn: String,
  val status: DtrStatus,
  val submission: DtrSubmissionDto?,
)

data class DtrSubmissionDto(
  val id: UUID,
  val localAuthorityAreaId: UUID,
  val referenceNumber: String?,
  val submissionDate: LocalDate,
  val createdBy: String,
  @field:JsonFormat(
    shape = JsonFormat.Shape.STRING,
    pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
    timezone = "UTC",
  )
  val createdAt: Instant,
)

data class DtrCommand(
  val localAuthorityAreaId: UUID,
  val referenceNumber: String?,
  val submissionDate: LocalDate,
  val status: DtrStatus,
)

enum class DtrStatus {
  NOT_STARTED,
  SUBMITTED,
  ACCEPTED,
  NOT_ACCEPTED,
}
