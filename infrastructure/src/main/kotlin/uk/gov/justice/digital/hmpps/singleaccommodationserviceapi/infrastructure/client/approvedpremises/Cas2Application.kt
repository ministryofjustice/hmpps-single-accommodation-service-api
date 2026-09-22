package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.util.requireXor
import java.time.OffsetDateTime
import java.util.UUID

data class Cas2Application(
  val uiUrl: String,
  val id: UUID,
  val createdAt: OffsetDateTime,
  val createdBy: Cas2Staff,
  val submittedApplication: Cas2SubmittedApplicationSummary?,
)

data class Cas2Staff(
  val name: String,
  val username: String,
  val deliusStaffCode: String?,
  val nomisStaffId: Long?,
  val userType: Cas2UserType,
)

enum class Cas2UserType {
  NOMIS,
  DELIUS,
  EXTERNAL,
}

data class Cas2SubmittedApplicationSummary(
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
