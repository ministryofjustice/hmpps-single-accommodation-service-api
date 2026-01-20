package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client

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
  const val GET_CAS_2_HDC_APPLICATION = "getCas2HdcApplication"
  const val GET_CAS_2_COURT_BAIL_APPLICATION = "getCas2CourtBailApplication"
  const val GET_CAS_2_PRISON_BAIL_APPLICATION = "getCas2PrisonBailApplication"
  const val GET_CAS_3_APPLICATION = "getCas3Application"

  // prisoner-search service
  const val GET_PRISONER = "getPrisoner"

  // approved-premises service - referrals
  const val GET_CAS1_REFERRAL = "getCas1ReferralByCrn"
  const val GET_CAS2_REFERRAL = "getCas2ReferralByCrn"
  const val GET_CAS2V2_REFERRAL = "getCas2v2ReferralByCrn"
  const val GET_CAS3_REFERRAL = "getCas3ReferralByCrn"

  const val GET_SUITABLE_CAS1_APPLICATION = "getSuitableCas1ApplicationByCrn"
  const val GET_SUITABLE_CAS2_HDC_APPLICATION = "getSuitableCas2HdcApplicationByCrn"
  const val GET_SUITABLE_CAS2_COURT_BAIL_APPLICATION = "getSuitableCas2CourtBailApplicationByCrn"
  const val GET_SUITABLE_CAS2_PRISON_BAIL_APPLICATION = "getSuitableCas2PrisonBailApplicationByCrn"
  const val GET_SUITABLE_CAS3_APPLICATION = "getSuitableCas3ApplicationByCrn"

  // accommodation-data-domain service
  const val GET_DUTY_TO_REFER = "getDutyToReferByCrn"
  const val GET_CRS = "getCrsByCrn"
  const val GET_CURRENT_ACCOMMODATION = "getCurrentAccommodation"
  const val GET_PROPOSED_ACCOMMODATIONS = "getProposedAccommodations"
}
