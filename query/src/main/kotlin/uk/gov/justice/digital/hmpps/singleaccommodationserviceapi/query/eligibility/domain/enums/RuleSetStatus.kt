package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums

enum class CaseStatus(val caseStatusOrder: Int) {
  NO_ACTION_NEEDED(0),
  ACTION_UPCOMING(1),
  ACTION_NEEDED(2),
}
