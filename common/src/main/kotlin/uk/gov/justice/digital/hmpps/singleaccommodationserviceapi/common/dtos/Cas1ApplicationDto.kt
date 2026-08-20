package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import com.fasterxml.jackson.annotation.JsonCreator
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class Cas1ApplicationDto(
  val uiUrl: String,
  val application: Cas1ApplicationSummaryDto,
  val assessment: Cas1AssessmentSummaryDto?,
  val requestForPlacement: Cas1RequestForPlacementSummaryDto?,
  val placement: Cas1PlacementSummaryDto?,
  val placementHistory: List<Cas1PlacementPairDto>,

  @Deprecated("This field will be removed once SAS is updated to use application.id")
  val id: UUID,

  @Deprecated("This field will be removed once SAS is updated to use application.status")
  val applicationStatus: Cas1ApplicationStatus,

  @Deprecated("This field will be removed once SAS is updated to use requestForPlacement.status")
  val requestForPlacementStatus: Cas1RequestForPlacementStatus?,

  @Deprecated("This field will be removed once SAS is updated to use placement.status")
  val placementStatus: Cas1PlacementStatus?,
)

data class Cas1PlacementPairDto(
  val requestForPlacement: Cas1RequestForPlacementSummaryDto?,
  val placement: Cas1PlacementSummaryDto?,
  val dateApplied: LocalDate,
)

data class Cas1ApplicationSummaryDto(
  val id: UUID,
  val status: Cas1ApplicationStatus,
  val createdAt: OffsetDateTime,
  val createdBy: Cas1StaffDto,
  val submittedAt: OffsetDateTime?,
  val expiresAt: LocalDate?,
)

data class Cas1AssessmentSummaryDto(
  val decision: AssessmentDecision?,
  val rejectionRationale: String?,
)

enum class AssessmentDecision {
  ACCEPTED,
  REJECTED,
}

data class Cas1RequestForPlacementSummaryDto(
  val status: Cas1RequestForPlacementStatus?,
  val decision: PlacementApplicationDecision?,
  val rejectionReason: String?,
  val submittedBy: Cas1StaffDto?,
  val submittedAt: LocalDate?,
  val withdrawalReason: WithdrawPlacementRequestReason?,
  val withdrawalDate: LocalDate?,
  val expectedArrivalDate: LocalDate?,
  val durationDays: Int?,
)

enum class PlacementApplicationDecision {
  ACCEPTED,
  REJECTED,
  WITHDRAW,
  WITHDRAWN_BY_PP,
}

enum class WithdrawPlacementRequestReason {
  DUPLICATE_PLACEMENT_REQUEST,
  ALTERNATIVE_PROVISION_IDENTIFIED,
  CHANGE_IN_CIRCUMSTANCES,
  CHANGE_IN_RELEASE_DECISION,
  NO_CAPACITY_DUE_TO_LOST_BED,
  NO_CAPACITY_DUE_TO_PLACEMENT_PRIORITISATION,
  NO_CAPACITY,
  ERROR_IN_PLACEMENT_REQUEST,
  WITHDRAWN_BY_PP,
  RELATED_APPLICATION_WITHDRAWN,
  RELATED_PLACEMENT_REQUEST_WITHDRAWN,
  RELATED_PLACEMENT_APPLICATION_WITHDRAWN,
}

data class Cas1PlacementSummaryDto(
  val status: Cas1PlacementStatus?,
  val actualArrivalDate: LocalDate?,
  val actualDepartureDate: LocalDate?,
  val cancellationReason: String?,
  val premises: Cas1PremisesSummaryDto?,
)

data class Cas1PremisesSummaryDto(
  val startDate: LocalDate?,
  val endDate: LocalDate?,
  val addressLine1: String,
  val addressLine2: String?,
  val town: String?,
  val postcode: String,
)

data class Cas1StaffDto(
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
