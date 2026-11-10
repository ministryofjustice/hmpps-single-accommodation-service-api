package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import com.fasterxml.jackson.annotation.JsonCreator
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

data class Cas1ApplicationV2(
  val uiUrl: String,
  val id: UUID,
  val status: Cas1ApplicationStatus,
  val createdAt: OffsetDateTime,
  val createdBy: Cas1Staff,
  val expiresAt: LocalDate?,
  val submittedApplication: Cas1SubmittedApplication?,
)

data class Cas1SubmittedApplication(
  val submittedAt: OffsetDateTime,
  val assessment: Cas1AssessmentSummary?,
  val upcomingRequestForPlacement: Cas1ExternalRequestForPlacementOutcome?,
  val requestForPlacementHistory: List<Cas1ExternalRequestForPlacementOutcome>,
)

data class Cas1ExternalRequestForPlacementOutcome(
  val requestForPlacement: Cas1RequestForPlacementSummaryV2,
  val placement: Cas1PlacementV2?,
)

data class Cas1RequestForPlacementSummaryV2(
  val status: Cas1RequestForPlacementStatus,
  val statusLastUpdated: LocalDateTime,
  val submittedBy: Cas1Staff,
  val submittedAt: LocalDate,
  val expectedArrivalDate: LocalDate,
  val durationDays: Int?,
  val decision: String?,
  val rejectionReason: String?,
  val withdrawal: Cas1RequestForPlacementWithdrawal?,
)

data class Cas1RequestForPlacementWithdrawal(
  val withdrawalReason: String?,
  val withdrawalDate: LocalDate?,
)

data class Cas1PlacementV2(
  val status: Cas1PlacementStatus,
  val statusLastUpdated: LocalDateTime,
  val canonicalArrivalDate: LocalDate,
  val canonicalDepartureDate: LocalDate,
  val actualArrivalDate: LocalDate?,
  val actualDepartureDate: LocalDate?,
  val expectedArrivalDate: LocalDate,
  val expectedDepartureDate: LocalDate,
  val cancellationReason: String?,
  val premises: Cas1ExternalPremisesV2Dto,
)

data class Cas1ExternalPremisesV2Dto(
  val addressLine1: String,
  val addressLine2: String?,
  val town: String?,
  val postcode: String,
)

data class Cas1AssessmentSummary(
  val decision: String?,
  val rejectionRationale: String?,
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
