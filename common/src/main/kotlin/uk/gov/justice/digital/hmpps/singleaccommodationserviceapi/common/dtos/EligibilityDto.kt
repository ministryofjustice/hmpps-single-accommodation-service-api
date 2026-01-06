package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.util.UUID

data class EligibilityDto(
  val crn: String,
  val cas1: ServiceResult,
  val cas2Hdc: ServiceResult?,
  val cas2PrisonBail: ServiceResult?,
  val cas2CourtBail: ServiceResult?,
  val cas3: ServiceResult?,
  val caseActions: List<String>,
  val caseStatus: CaseStatus,
)

data class ServiceResult(
  val serviceStatus: ServiceStatus,
  val suitableApplication: SuitableApplication? = null,
  val actions: List<String>,
)

data class SuitableApplication(
  val id: UUID,
  val applicationStatus: String,
  val placementStatus: String?,
)

enum class CaseStatus(val caseStatusOrder: Int) {
  NO_ACTION_NEEDED(0),
  ACTION_UPCOMING(1),
  ACTION_NEEDED(2),
}

enum class ServiceStatus {
  NOT_STARTED, // NO APPLICATION
  NOT_ELIGIBLE, // NO APPLICATION
  UPCOMING, // NO APPLICATION
  AWAITING_ASSESSMENT, // SUITABLE APPLICATION, PLACEMENT NEEDED
  UNALLOCATED_ASSESSMENT, // SUITABLE APPLICATION, PLACEMENT NEEDED
  ASSESSMENT_IN_PROGRESS, // SUITABLE APPLICATION, PLACEMENT NEEDED
  AWAITING_PLACEMENT, // SUITABLE APPLICATION, PLACEMENT NEEDED
  REQUEST_FOR_FURTHER_INFORMATION, // SUITABLE APPLICATION, PLACEMENT NEEDED
  PENDING_PLACEMENT_REQUEST, // SUITABLE APPLICATION, PLACEMENT NEEDED
  ARRIVED, // SUITABLE APPLICATION AND NO ACTION NEEDED
  UPCOMING_PLACEMENT, // SUITABLE APPLICATION AND NO ACTION NEEDED
  DEPARTED, // SUITABLE APPLICATION, PLACEMENT NEEDED
  NOT_ARRIVED, // SUITABLE APPLICATION, PLACEMENT NEEDED
  CANCELLED, // SUITABLE APPLICATION, PLACEMENT NEEDED
}