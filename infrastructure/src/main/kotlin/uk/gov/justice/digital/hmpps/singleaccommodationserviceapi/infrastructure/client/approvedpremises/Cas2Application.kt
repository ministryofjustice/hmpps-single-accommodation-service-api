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
  val cohort: String?,
)

data class Cas2Staff(
  val name: String,
  val username: String,
  val deliusStaffCode: String?,
  val nomisStaffId: Long?,
  val userType: String,
)

data class Cas2SubmittedApplicationSummary(
  val latestAssessmentStatus: Cas2AssessmentStatus?,
  val offerDeclinedReason: String?,
  val cancelledReason: String?,
  val submittedAt: OffsetDateTime,
) {
  init {
    requireXor(
      latestAssessmentStatus == Cas2AssessmentStatus.CANCELLED,
      cancelledReason == null,
    ) {
      "Cancelled reason is required when status is `cancelled` and forbidden otherwise"
    }
  }

  init {
    requireXor(
      latestAssessmentStatus == Cas2AssessmentStatus.OFFER_DECLINED,
      offerDeclinedReason == null,
    ) {
      "Offer declined reason is required when status is `offerDeclined` and forbidden otherwise"
    }
  }
}
