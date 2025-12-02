package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain

data class ServiceResult(
  val failedResults: List<RuleResult>,
  val serviceStatus: ServiceStatus,
  val action: String,
)

enum class ServiceStatus(val value: String) {
  // show when:
  // eligible for CAS1
  // and within 6 months of release
  // CAS1 referral has not been submitted yet (relevant to this release)
  // action is Start approved premise referral
  NOT_STARTED("Not Started"),

  // show when:
//  eligible for CAS1
//  and NOT within 6 months of release
//  CAS1 referral has not been submitted yet (relevant to this release)
  // action is: Start approved premise referral in X days
  UPCOMING("Upcoming"),
  // show when has a submitted CAS1 application (that isn't booked in yet

  SUBMITTED("Submitted"),

  // show when has a CAS1 placement in the future //////
  CONFIRMED("Confirmed"),

  // show when:
//  not eligible for CAS1
//  no existing booking or application ///////
  NOT_ELIGIBLE("Not Eligible"),
}
