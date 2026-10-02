package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.OffsetDateTime
import java.util.UUID

data class Cas2ApplicationDto(
  val uiUrl: String,
  val id: UUID,
  val createdAt: OffsetDateTime,
  val createdBy: Cas2StaffDto,
  val submittedApplication: Cas2SubmittedApplicationSummaryDto?,
  val cohort: Cas2CohortDto?,
)

data class Cas2StaffDto(
  val name: String,
  val username: String,
  val deliusStaffCode: String?,
  val nomisStaffId: Long?,
  val userType: String,
)

enum class Cas2CohortDto {
  ALTERNATIVE_TO_CUSTODIAL_RECALL,
  HOMELESS_AT_CONDITIONAL_RELEASE_DATE,
  HOMELESS_AT_END_OF_FIXED_TERM_RECALL,
  INTENSIVE_SUPERVISION_COURTS,
  RISK_ASSESSED_RECALL_REVIEW,
  REFERRAL_FROM_APPROVED_PREMISES,
  UNKNOWN,
}

enum class Cas2AssessmentStatusDto {
  MORE_INFO_REQUESTED,
  AWAITING_DECISION,
  ON_WAITING_LIST,
  PLACE_OFFERED,
  OFFER_ACCEPTED,
  OFFER_DECLINED,
  WITHDRAWN,
  CANCELLED,
  AWAITING_ARRIVAL,
  UNKNOWN,
}

data class Cas2SubmittedApplicationSummaryDto(
  val latestAssessmentStatus: Cas2AssessmentStatusDto?,
  val offerDeclinedReason: String?,
  val cancelledReason: String?,
  val submittedAt: OffsetDateTime,
)
