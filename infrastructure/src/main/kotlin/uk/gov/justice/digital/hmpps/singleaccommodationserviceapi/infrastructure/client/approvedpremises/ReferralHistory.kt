package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import java.time.Instant
import java.util.UUID

sealed interface CasReferralHistory

data class Cas1ReferralHistory(
  val id: UUID,
  val applicationId: UUID,
  val applicationStatus: ApprovedPremisesApplicationStatus,
  val requestForPlacementStatus: RequestForPlacementStatus?,
  val createdAt: Instant,
  val referralRejectionReason: String?,
  val referralRejectionReasonDetail: String?,
  val localAuthorityArea: String?,
  val pdu: String?,
  val referredBy: DeliusUserDto,
  val placementAddress: String?,
  val placementStatus: Cas1SpaceBookingStatus?,
  val uiUrl: String,
) : CasReferralHistory {
  enum class ApprovedPremisesApplicationStatus(val value: String) {
    STARTED("started"),
    REJECTED("rejected"),
    AWAITING_ASSESSMENT("awaitingAssesment"),
    UNALLOCATED_ASSESSMENT("unallocatedAssesment"),
    ASSESSMENT_IN_PROGRESS("assesmentInProgress"),
    AWAITING_PLACEMENT("awaitingPlacement"),
    REQUESTED_FURTHER_INFORMATION("requestedFurtherInformation"),
    PENDING_PLACEMENT_REQUEST("pendingPlacementRequest"),
    PLACEMENT_ALLOCATED("placementAllocated"),
    INAPPLICABLE("inapplicable"),
    WITHDRAWN("withdrawn"),
    EXPIRED("expired"),
    ;

    companion object {
      @JvmStatic
      @JsonCreator
      fun forValue(value: String): ApprovedPremisesApplicationStatus = entries.firstOrNull { it.name == value } ?: throw IllegalArgumentException("Unknown value: $value")
    }
  }

  enum class RequestForPlacementStatus(@get:JsonValue val value: String) {
    REQUEST_UNSUBMITTED("request_unsubmitted"),
    REQUEST_REJECTED("request_rejected"),
    REQUEST_SUBMITTED("request_submitted"),
    AWAITING_MATCH("awaiting_match"),
    REQUEST_WITHDRAWN("request_withdrawn"),
    PLACEMENT_BOOKED("placement_booked"),
    ;

    companion object {
      @JvmStatic
      @JsonCreator
      fun forValue(value: String): RequestForPlacementStatus = entries.firstOrNull { it.value == value } ?: throw IllegalArgumentException("Unknown value: $value")
    }
  }

  enum class Cas1SpaceBookingStatus(@get:JsonValue val value: String) {
    CANCELLED("cancelled"),
    NOT_ARRIVED("notArrived"),
    DEPARTED("departed"),
    ARRIVED("arrived"),
    UPCOMING("upcoming"),
    ;

    companion object {
      @JvmStatic
      @JsonCreator
      fun forValue(value: String): Cas1SpaceBookingStatus = entries.firstOrNull { it.value == value } ?: throw IllegalArgumentException("Unknown value: $value")
    }
  }
}

data class Cas3ReferralHistory(
  val id: UUID,
  val applicationId: UUID,
  val applicationStatus: ApplicationStatus,
  val createdAt: Instant,
  val referralRejectionReason: String?,
  val referralRejectionReasonDetail: String?,
  val localAuthorityArea: String?,
  val pdu: String?,
  val referredBy: DeliusUserDto,
  val placementAddress: String?,
  val bookingStatus: Cas3BookingStatus?,
  val uiUrl: String,
) : CasReferralHistory {

  enum class ApplicationStatus(@get:JsonValue val value: String) {
    REJECTED("rejected"),
    IN_PROGRESS("inProgress"),
    SUBMITTED("submitted"),
    REQUESTED_FURTHER_INFORMATION("requestedFurtherInformation"),
    ;

    companion object {
      @JvmStatic
      @JsonCreator
      fun forValue(value: String): ApplicationStatus = entries.firstOrNull { it.value == value } ?: throw IllegalArgumentException("Unknown value: $value")
    }
  }

  enum class Cas3BookingStatus(@get:JsonValue val value: String) {
    ARRIVED("arrived"),
    NOT_MINUS_ARRIVED("notMinusArrived"),
    DEPARTED("departed"),
    CANCELLED("cancelled"),
    PROVISIONAL("provisional"),
    CONFIRMED("confirmed"),
    CLOSED("closed"),
    ;

    companion object {
      @JvmStatic
      @JsonCreator
      fun forValue(value: String): Cas3BookingStatus = entries.firstOrNull { it.value == value } ?: throw IllegalArgumentException("Unknown value: $value")
    }
  }
}

data class DeliusUserDto(
  val name: String,
  val username: String? = null, // TODO make this non-nullable when refactoring
  val staffCode: String? = null,
)
