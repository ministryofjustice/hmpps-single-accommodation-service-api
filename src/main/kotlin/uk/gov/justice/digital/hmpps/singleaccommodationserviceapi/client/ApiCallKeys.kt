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

  // approved-premises service - referrals
  const val GET_CAS1_REFERRAL = "getCas1Referral"
  const val GET_CAS2_REFERRAL = "getCas2Referral"
  const val GET_CAS2V2_REFERRAL = "getCas2v2Referral"
  const val GET_CAS3_REFERRAL = "getCas3Referral"

}
