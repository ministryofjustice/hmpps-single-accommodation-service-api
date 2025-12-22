package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client

object ApiCallKeys {
  // probation-integration AP and Delius service
  const val GET_CASE_SUMMARIES = "getCaseSummaries"
  const val GET_CASE_SUMMARY = "getCaseSummaryByCrn"

  // probation-integration Oasys service
  const val GET_ROSH_DETAIL = "getRoshSummaryByCrn"

  // core-person-record service
  const val GET_CORE_PERSON_RECORD = "getCorePersonRecordByCrn"

  // tier service
  const val GET_TIER = "getTierByCrn"

  // Approved Premises service
  const val GET_ACCOMMODATION_RESPONSE = "getAccommodationResponse"
  const val GET_CAS_1_APPLICATION = "getCas1Application"

  // prisoner-search service
  const val GET_PRISONER = "getPrisoner"

  // approved-premises service - referrals
  const val GET_CAS1_REFERRAL = "getCas1ReferralByCrn"
  const val GET_CAS2_REFERRAL = "getCas2ReferralByCrn"
  const val GET_CAS2V2_REFERRAL = "getCas2v2ReferralByCrn"
  const val GET_CAS3_REFERRAL = "getCas3ReferralByCrn"

  const val GET_SUITABLE_CAS1_APPLICATION = "getSuitableCas1ApplicationByCrn"
}
