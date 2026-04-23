package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client

object ApiCallKeys {
  // probation-integration AP and Delius service
  const val GET_CASE_SUMMARY = "getCaseSummaryByCrn"
  const val GET_STAFF_DETAIL = "getStaffDetailByUsername"

  // probation-integration SAS and Delius service
  const val GET_CASE_LIST = "getCaseListByUsername"

  // probation-integration Oasys service
  const val GET_ROSH_DETAIL = "getRoshSummaryByCrn"

  // core-person-record service
  const val GET_CORE_PERSON_RECORD_BY_CRN = "getCorePersonRecordByCrn"
  const val GET_CORE_PERSON_RECORD_BY_PRISON_NUMBER = "getCorePersonRecordByPrisonNumber"
  const val GET_CORE_PERSON_RECORD_ADDRESSES_BY_CRN = "getCorePersonRecordAddressesByCrn"

  // CRS service
  const val GET_CRS = "getCrsByCrn"

  // tier service
  const val GET_TIER = "getTierByCrn"

  // Approved Premises service
  const val GET_ACCOMMODATION_RESPONSE = "getAccommodationResponse"
  const val GET_CAS_1_APPLICATION = "getCas1Application"
  const val GET_CAS_3_APPLICATION = "getCas3Application"

  // approved-premises service - referrals
  const val GET_CAS1_REFERRAL = "getCas1ReferralByCrn"
  const val GET_CAS2_REFERRAL = "getCas2ReferralByCrn"
  const val GET_CAS2V2_REFERRAL = "getCas2v2ReferralByCrn"
  const val GET_CAS3_REFERRAL = "getCas3ReferralByCrn"
}
