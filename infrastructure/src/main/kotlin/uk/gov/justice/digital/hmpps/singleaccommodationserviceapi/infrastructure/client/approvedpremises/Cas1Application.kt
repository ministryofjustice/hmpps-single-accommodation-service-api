package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import com.fasterxml.jackson.annotation.JsonCreator
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class Cas1Application(
  val uiUrl: String,
  val application: Cas1ApplicationSummary,
  val assessment: Cas1AssessmentSummary?,
  val requestForPlacement: Cas1RequestForPlacementSummary?,
  val placement: Cas1PlacementSummary?,
  val placementHistory: List<Cas1PlacementPair>,
)

data class Cas1PlacementPair(
  val requestForPlacement: Cas1RequestForPlacementSummary?,
  val placement: Cas1PlacementSummary?,
  val dateApplied: LocalDate,
)

data class Cas1ApplicationSummary(
  val id: UUID,
  val status: Cas1ApplicationStatus,
  val createdAt: OffsetDateTime,
  val createdBy: Cas1Staff,
  val submittedAt: OffsetDateTime?,
  val expiresAt: LocalDate?,
)

data class Cas1AssessmentSummary(
  val decision: String?,
  val rejectionRationale: String?,
)

data class Cas1RequestForPlacementSummary(
  val status: Cas1RequestForPlacementStatus?,
  val decision: String?,
  val rejectionReason: String?,
  val submittedBy: Cas1Staff?,
  val submittedAt: LocalDate?,
  val withdrawalReason: String?,
  val withdrawalDate: LocalDate?,
  val expectedArrivalDate: LocalDate?,
  val durationDays: Int?,
)

data class Cas1PlacementSummary(
  val status: Cas1PlacementStatus?,
  val actualArrivalDate: LocalDate?,
  val actualDepartureDate: LocalDate?,
  val cancellationReason: String?,
  val premises: Cas1PremisesSummary?,
)

data class Cas1PremisesSummary(
  val startDate: LocalDate?,
  val endDate: LocalDate?,
  val addressLine1: String,
  val addressLine2: String?,
  val town: String?,
  val postcode: String,
)

data class Cas1Staff(
  val name: String,
  val username: String,
  val staffCode: String,
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
