package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import com.fasterxml.jackson.annotation.JsonCreator
import java.time.Instant
import java.util.UUID

sealed interface CasReferralHistory

data class Cas1ReferralHistory(
  val casService: CasService,
  val id: UUID,
  val applicationId: UUID,
  val status: Cas1AssessmentStatus,
  val createdAt: Instant,
) : CasReferralHistory {
  enum class Cas1AssessmentStatus(val value: String) {
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
        .first { it.value == value || it.name == value }
    }
  }
}

data class Cas2ReferralHistory(
  val casService: CasService,
  val id: UUID,
  val applicationId: UUID,
  val status: Cas2Status,
  val createdAt: Instant,
) : CasReferralHistory {
  enum class Cas2Status(val value: String) {
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
      fun forValue(value: String): Cas2Status = entries.first { it.value == value || it.name == value }
    }
  }
}

data class Cas3ReferralHistory(
  val casService: CasService,
  val id: UUID,
  val applicationId: UUID,
  val status: TemporaryAccommodationAssessmentStatus,
  val createdAt: Instant,
) : CasReferralHistory {
  enum class TemporaryAccommodationAssessmentStatus(val value: String) {

    UNALLOCATED("unallocated"),
    IN_REVIEW("in_review"),
    READY_TO_PLACE("ready_to_place"),
    CLOSED("closed"),
    REJECTED("rejected"),
    ;

    companion object {
      @JvmStatic
      @JsonCreator
      fun forValue(value: String): TemporaryAccommodationAssessmentStatus = entries.first { it.value == value || it.name == value }
    }
  }
}
