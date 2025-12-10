package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client

object ApiCallKeys {
  // probation-integration AP and Delius service
  const val GET_CASE_SUMMARIES = "getCaseSummaries"
  const val GET_CASE_SUMMARY = "getCaseSummary"

  // probation-integration Oasys service
  const val GET_ROSH_DETAIL = "getRoshDetails"

  // core-person-record service
  const val GET_CORE_PERSON_RECORD = "getCorePersonRecord"

  // tier service
  const val GET_TIER = "getTier"

  // Approved Premises service
  const val GET_ACCOMMODATION_STATUS = "getAccommodationStatus"

  // prisoner-search service
  const val GET_PRISONER = "getPrisoner"
}
