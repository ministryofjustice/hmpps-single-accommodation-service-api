package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class DutyToReferDto(
  val caseId: UUID,
  val crn: String,
  val status: DtrStatus,
  val submission: DtrSubmissionDto?,
)

data class DtrSubmissionDto(
  val id: UUID,
  val localAuthority: LocalAuthorityDto,
  val referenceNumber: String?,
  val submissionDate: LocalDate,
  val createdBy: String, // TODO: this should be a user object. Refactor to make username non-nullable.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  val createdByUsername: String? = null,
  @field:JsonFormat(
    shape = JsonFormat.Shape.STRING,
    pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
    timezone = "UTC",
  )
  val createdAt: Instant,
  val withdrawalReason: WithdrawalReason? = null,
  val withdrawalReasonOther: String? = null,
  val outcomeReason: OutcomeReason? = null,
  val submissionNote: String? = null,
  val outcomeNote: String? = null,
)

data class LocalAuthorityDto(
  val localAuthorityAreaId: UUID,
  val localAuthorityAreaName: String?,
)

data class DtrCommand(
  val localAuthorityAreaId: UUID,
  val referenceNumber: String?,
  val submissionDate: LocalDate,
  val status: DtrStatus,
  val withdrawalReason: WithdrawalReason? = null,
  val withdrawalReasonOther: String? = null,
  val outcomeReason: OutcomeReason? = null,
  val submissionNote: String? = null,
  val outcomeNote: String? = null,
)

enum class DtrStatus {
  SUBMITTED,
  ACCEPTED,
  NOT_ACCEPTED,
  WITHDRAWN,
}

enum class WithdrawalReason {
  NEW_REFERRAL,
  INCORRECT_LOCAL_AUTHORITY,
  NO_CONSENT,
  DISENGAGED,
  HOUSING_NEED_RESOLVED,
  NOT_ELIGIBLE,
  OTHER,
}

enum class OutcomeReason {
  PREVENTION_AND_RELIEF_DUTY,
  PRIORITY_NEED,
  NO_LOCAL_CONNECTION,
  INTENTIONALLY_HOMELESS,
  REJECTED_FOR_ANOTHER_REASON,
}
