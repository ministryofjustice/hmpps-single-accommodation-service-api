package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import java.time.Instant
import java.util.UUID

data class ReferralHistory<T : CasStatus>(
  val casService: CasService,
  val id: UUID,
  val applicationId: UUID,
  val status: T,
  val createdAt: Instant,
)

sealed interface CasStatus

enum class Cas1AssessmentStatus(@JsonValue val value: String) : CasStatus {
  AWAITING_RESPONSE("awaiting_response"),
  COMPLETED("completed"),
  REALLOCATED("reallocated"),
  IN_PROGRESS("in_progress"),
  NOT_STARTED("not_started"),
  ;

  companion object {
    @JvmStatic
    @JsonCreator
    fun forValue(value: String): Cas1AssessmentStatus = entries
      .first { it.value == value }
  }
}

enum class Cas2Status(@JsonValue val value: String) : CasStatus {
  MORE_INFORMATION_REQUESTED("More information requested"),
  PLACE_OFFERED("Place offered"),
  AWAITING_ARRIVAL("Awaiting arrival"),
  REFERRAL_CANCELLED("Referral cancelled"),
  REFERRAL_WITHDRAWN("Referral withdrawn"),
  OFFER_ACCEPTED("Offer accepted"),
  ON_WAITING_LIST("On waiting list"),
  AWAITING_DECISION("Awaiting decision"),
  OFFER_DECLINED_OR_WITHDRAWN("Offer declined or withdrawn"),
  ;

  companion object {
    @JvmStatic
    @JsonCreator
    fun forValue(value: String): Cas2Status = entries.first { it.value == value }
  }
}

enum class TemporaryAccommodationAssessmentStatus(@JsonValue val value: String) : CasStatus {

  UNALLOCATED("unallocated"),
  IN_REVIEW("in_review"),
  READY_TO_PLACE("ready_to_place"),
  CLOSED("closed"),
  REJECTED("rejected"),
  ;

  companion object {
    @JvmStatic
    @JsonCreator
    fun forValue(value: String): TemporaryAccommodationAssessmentStatus = entries.first { it.value == value }
  }
}
