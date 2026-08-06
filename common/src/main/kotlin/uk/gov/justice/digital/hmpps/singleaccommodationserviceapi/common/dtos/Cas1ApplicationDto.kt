package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import com.fasterxml.jackson.annotation.JsonCreator
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class Cas1ApplicationDto(
  val id: UUID,
  val uiUrl: String,

// application info
  val applicationStatus: Cas1ApplicationStatus,
  val applicationStartedAt: OffsetDateTime,
  val applicationStartedBy: String,
  val applicationSubmittedAt: OffsetDateTime?,
  val applicationSubmittedBy: String?,
  val applicationExpiresAt: LocalDate?,

// assessment info
  val assessmentDecision: String?,
  val assessmentRejectionRationale: String?,

// request for placement info
  val requestForPlacementStatus: Cas1RequestForPlacementStatus?,
  val requestForPlacementDecision: String?,
  val requestForPlacementRejectionReason: String?,
  val requestSubmittedBy: String?,
  val requestSubmittedAt: LocalDate?,
  val withdrawalReason: String?,
  val withdrawalDate: LocalDate?,

// placement info
  val placementStatus: Cas1PlacementStatus?,
  val premises: Cas1PremisesSummaryDto?,
  val actualArrivalDate: LocalDate?,
  val actualDepartureDate: LocalDate?,
  val cancellationReason: String?,

// multiple
  val expectedArrivalDate: LocalDate?,
  val duration: Int?,
)

data class Cas1PremisesSummaryDto(
  val startDate: LocalDate?,
  val endDate: LocalDate?,
  val addressLine1: String,
  val addressLine2: String?,
  val town: String?,
  val postcode: String,
)

enum class Cas1RequestForPlacementStatus(val casValue: String) {
  REQUEST_UNSUBMITTED("request_unsubmitted"),
  REQUEST_REJECTED("request_rejected"),
  REQUEST_SUBMITTED("request_submitted"),
  AWAITING_MATCH("awaiting_match"),
  REQUEST_WITHDRAWN("request_withdrawn"),
  PLACEMENT_BOOKED("placement_booked"),
  ;

  companion object {
    @JsonCreator
    @JvmStatic
    fun from(value: String): Cas1RequestForPlacementStatus = Cas1RequestForPlacementStatus.entries.first { it.casValue == value || it.name == value }
  }
}

enum class Cas1PlacementStatus(val casValue: String) {
  ARRIVED("arrived"),
  UPCOMING("upcoming"),
  DEPARTED("departed"),
  NOT_ARRIVED("notArrived"),
  CANCELLED("cancelled"),
  ;

  companion object {
    @JsonCreator
    @JvmStatic
    fun from(value: String): Cas1PlacementStatus = Cas1PlacementStatus.entries.first { it.casValue == value || it.name == value }
  }
}

enum class Cas1ApplicationStatus {
  AWAITING_ASSESSMENT,
  UNALLOCATED_ASSESSMENT,
  ASSESSMENT_IN_PROGRESS,
  AWAITING_PLACEMENT,
  PLACEMENT_ALLOCATED,
  REQUESTED_FURTHER_INFORMATION,
  PENDING_PLACEMENT_REQUEST,
  STARTED,
  REJECTED,
  INAPPLICABLE,
  WITHDRAWN,
  EXPIRED,
}
