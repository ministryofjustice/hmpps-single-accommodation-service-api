package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.util.requireXor
import java.time.OffsetDateTime
import java.util.UUID

data class Cas2ApplicationDto(
  val uiUrl: String,
  val id: UUID,
  val createdAt: OffsetDateTime,
  val createdBy: Cas2StaffDto,
  val submittedApplication: Cas2SubmittedApplicationSummaryDto?,
)

data class Cas2StaffDto(
  val name: String,
  val username: String,
  val deliusStaffCode: String?,
  val nomisStaffId: Long?,
  val userType: Cas2UserTypeDto,
)

enum class Cas2UserTypeDto {
  NOMIS,
  DELIUS,
  EXTERNAL,
}

data class Cas2SubmittedApplicationSummaryDto(
  val latestAssessmentStatus: String?,
  val offerDeclinedReason: String?,
  val cancelledReason: String?,
  val submittedAt: OffsetDateTime,
) {
  init {
    requireXor(
      latestAssessmentStatus == "cancelled",
      cancelledReason == null,
    ) {
      "Cancelled reason can only be provided if status is `cancelled`"
    }
  }

  init {
    requireXor(
      latestAssessmentStatus == "offerDeclined",
      offerDeclinedReason == null,
    ) {
      "Cancelled reason can only be provided if status is `offerDeclined`"
    }
  }
}
