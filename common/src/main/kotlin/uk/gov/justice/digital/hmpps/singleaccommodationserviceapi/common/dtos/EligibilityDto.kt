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
  val suitableApplicationId: UUID? = null,
  val action: RuleAction? = null,
)

data class RuleAction(
  val text: String,
  val isUpcoming: Boolean = false,
)

enum class CaseStatus(val caseStatusOrder: Int) {
  NO_ACTION_NEEDED(0),
  ACTION_UPCOMING(1),
  ACTION_NEEDED(2),
}

enum class ServiceStatus {
  NOT_ELIGIBLE, // NO APPLICATION
  UPCOMING, // NO APPLICATION
  NOT_STARTED,
  REJECTED,
  WITHDRAWN,
  SUBMITTED,
  CONFIRMED,
}
