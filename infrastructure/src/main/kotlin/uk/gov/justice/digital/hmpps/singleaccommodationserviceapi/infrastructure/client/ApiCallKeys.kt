package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client

object ApiCallKeys {
  // probation-integration AP and Delius service
  const val GET_CASE_SUMMARIES = "getCaseSummaries"
  const val GET_CASE_SUMMARY = "getCaseSummaryByCrn"
  const val GET_STAFF_DETAIL = "getStaffDetailByUsername"

  // probation-integration SAS and Delius service
  const val GET_CASE_LIST = "getCaseListByUsername"

  // probation-integration Oasys service
  const val GET_ROSH_DETAIL = "getRoshSummaryByCrn"
  const val GET_ROSH_DETAILS = "getRoshSummariesByCrns"

  // core-person-record service
  const val GET_CORE_PERSON_RECORD = "getCorePersonRecordByCrn"
  const val GET_CORE_PERSON_RECORDS = "getCorePersonRecordsByCrns"

  // tier service
  const val GET_TIER = "getTierByCrn"
  const val GET_TIERS = "getTiersByCrns"

  // Approved Premises service
  const val GET_ACCOMMODATION_RESPONSE = "getAccommodationResponse"
  const val GET_CAS_1_APPLICATION = "getCas1Application"
  const val GET_CAS_2_HDC_APPLICATION = "getCas2HdcApplication"
  const val GET_CAS_2_COURT_BAIL_APPLICATION = "getCas2CourtBailApplication"
  const val GET_CAS_2_PRISON_BAIL_APPLICATION = "getCas2PrisonBailApplication"
  const val GET_CAS_1_APPLICATIONS = "getCas1Applications"
  const val GET_CAS_2_HDC_APPLICATIONS = "getCas2HdcApplications"
  const val GET_CAS_2_COURT_BAIL_APPLICATIONS = "getCas2CourtBailApplications"
  const val GET_CAS_2_PRISON_BAIL_APPLICATIONS = "getCas2PrisonBailApplications"

  // prisoner-search service
  const val GET_PRISONER = "getPrisoner"
  const val GET_PRISONERS = "getPrisoners"

  // approved-premises service - referrals
  const val GET_CAS1_REFERRAL = "getCas1ReferralByCrn"
  const val GET_CAS2_REFERRAL = "getCas2ReferralByCrn"
  const val GET_CAS2V2_REFERRAL = "getCas2v2ReferralByCrn"
  const val GET_CAS3_REFERRAL = "getCas3ReferralByCrn"
}
