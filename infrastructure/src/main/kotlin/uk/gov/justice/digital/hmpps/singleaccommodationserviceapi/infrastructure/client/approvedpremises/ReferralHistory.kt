package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import com.fasterxml.jackson.annotation.JsonCreator
import java.time.Instant
import java.util.UUID

sealed interface CasReferralHistory

data class Cas1ReferralHistory(
  val id: UUID,
  val applicationId: UUID,
  val status: Cas1AssessmentStatus,
  val createdAt: Instant,
  val referralRejectionReason: String?,
  val referralRejectionReasonDetail: String?,
  val localAuthorityArea: String?,
  val pdu: String?,
  val referredBy: DeliusUserDto,
  val placementAddress: String?,
  val placementStatus: String?,
  val referralUrl: String,
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

data class Cas3ReferralHistory(
  val id: UUID,
  val applicationId: UUID,
  val status: TemporaryAccommodationAssessmentStatus,
  val createdAt: Instant,
  val referralRejectionReason: String?,
  val referralRejectionReasonDetail: String?,
  val localAuthorityArea: String?,
  val pdu: String?,
  val referredBy: DeliusUserDto,
  val placementAddress: String?,
  val placementStatus: String?,
  val referralUrl: String,
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

data class DeliusUserDto(
  val name: String,
  val username: String? = null, // TODO make this non-nullable when refactoring
  val staffCode: String? = null,
)
