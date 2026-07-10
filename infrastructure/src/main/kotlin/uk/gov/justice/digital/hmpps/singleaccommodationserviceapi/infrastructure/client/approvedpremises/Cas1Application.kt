package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import com.fasterxml.jackson.annotation.JsonCreator
import java.time.LocalDate
import java.util.UUID

data class Cas1Application(
  val id: UUID,
  val applicationStatus: Cas1ApplicationStatus,
  val requestForPlacementStatus: Cas1RequestForPlacementStatus?,
  val placementStatus: Cas1PlacementStatus?,
  val premises: Cas1PremisesSummary?,
  val uiUrl: String?,
)

data class Cas1PremisesSummary(
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
  REQUEST_FOR_FURTHER_INFORMATION,
  PENDING_PLACEMENT_REQUEST,
  STARTED,
  REJECTED,
  INAPPLICABLE,
  WITHDRAWN,
  EXPIRED,
}
