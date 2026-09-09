package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class OutOfRegionReferralDto(
  val caseId: UUID,
  val crn: String,
  val status: OorStatus,
  val submission: OorSubmissionDto?,
  @JsonInclude(JsonInclude.Include.NON_NULL)
  val active: Boolean? = null,
)

data class OorSubmissionDto(
  val id: UUID,
  val referenceNumber: String?,
  val submissionDate: LocalDate,
  val createdBy: String,
  @JsonInclude(JsonInclude.Include.NON_NULL)
  val createdByUsername: String? = null,
  @field:JsonFormat(
    shape = JsonFormat.Shape.STRING,
    pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
    timezone = "UTC",
  )
  val createdAt: Instant,
  val outcomeReason: OutcomeReason? = null,
  val submissionNote: String? = null,
  val outcomeNote: String? = null,
)

data class OorCommand(
  val referenceNumber: String?,
  val submissionDate: LocalDate,
  val status: OorStatus,
  val outcomeReason: OutcomeReason? = null,
  val submissionNote: String? = null,
  val outcomeNote: String? = null,
)

enum class OorStatus(override val title: String) : TitleEnum {
  SUBMITTED("Submitted"),
  ACCEPTED("Accepted"),
  NOT_ACCEPTED("Not Accepted"),
}
