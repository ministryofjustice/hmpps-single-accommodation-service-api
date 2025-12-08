package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain

data class ServiceResult(
  val failedResults: List<RuleResult>,
  val serviceStatus: String,
  val actions: List<String>,
)

enum class ServiceStatus(val value: String) {
  NOT_STARTED("Not Started"),
  UPCOMING("Upcoming"),
  NOT_ELIGIBLE("Not Eligible"),
}
