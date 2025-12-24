package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums

enum class CaseStatus {
  ACTION_NEEDED,
  NO_ACTION_NEEDED,
  ACTION_UPCOMING,
  ;

  companion object {
    val caseStatusOrder = mapOf(
      NO_ACTION_NEEDED to 0,
      ACTION_UPCOMING to 1,
      ACTION_NEEDED to 2,
    )
  }
}
