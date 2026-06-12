package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client

object ApiCallKeys {
  // probation-integration AP and Delius service
  const val GET_CASE = "getCaseByCrn"
  const val GET_STAFF_DETAIL = "getStaffDetailByUsername"

  // probation-integration SAS and Delius service
  const val GET_CASE_LIST = "getCaseListByUsername"
  const val FULL_CASE_LIST = "fullCaseListByUsername"

  // probation-integration Oasys service
  const val GET_ROSH_DETAIL = "getRoshSummaryByCrn"

  // core-person-record service
  const val GET_CORE_PERSON_RECORD_BY_CRN = "getCorePersonRecordByCrn"
  const val GET_CORE_PERSON_RECORD_BY_PRISON_NUMBER = "getCorePersonRecordByPrisonNumber"

  // tier service
  const val GET_TIER = "getTierByCrn"

  // Approved Premises service
  const val GET_CAS_1_CURRENT_PREMISES = "getCas1CurrentPremises"
  const val GET_CAS_3_CURRENT_PREMISES = "getCas3CurrentPremises"
  const val GET_CAS_1_APPLICATION = "getCas1Application"
  const val GET_CAS_3_APPLICATION = "getCas3Application"

  // prisoner-search service
  const val GET_PRISONER = "getPrisoner"

  // commissioned rehabilitative services (CRS)
  const val GET_CRS = "getCrsByCrn"

  // approved-premises service - referrals
  const val GET_CAS1_REFERRAL = "getCas1ReferralByCrn"
  const val GET_CAS2HDC_REFERRAL = "getCas2HdcReferralByCrn"
  const val GET_CAS2_REFERRAL = "getCas2ReferralByCrn"
  const val GET_CAS3_REFERRAL = "getCas3ReferralByCrn"

  val listOf404sThatAreNotBreaking = listOf(
    GET_CAS1_REFERRAL, GET_CAS2_REFERRAL, GET_CAS2HDC_REFERRAL, GET_CAS3_REFERRAL, // cas referrals
    GET_CAS_1_CURRENT_PREMISES, GET_CAS_3_CURRENT_PREMISES, // cas premises
    GET_CAS_1_APPLICATION, GET_CAS_3_APPLICATION, // cas applications
    GET_PRISONER, // prisoner as them not having a prison record is probably not a problem
  )
}
