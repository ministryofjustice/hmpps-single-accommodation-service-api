package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys

interface ApprovedPremisesClient {

  @GetExchange(value = "/cas1/external/suitable-applications/{crn}")
  fun getSuitableCas1ApplicationInternal(@PathVariable crn: String): Cas1Application

  @GetExchange(value = "/cas1/external/referrals/{crn}")
  fun getCas1Referral(@PathVariable crn: String): List<ReferralHistory<Cas1AssessmentStatus>>

  @GetExchange(value = "/cas2/external/referrals/{crn}")
  fun getCas2Referral(@PathVariable crn: String): List<ReferralHistory<Cas2Status>>

  @GetExchange(value = "/cas2v2/external/referrals/{crn}")
  fun getCas2v2Referral(@PathVariable crn: String): List<ReferralHistory<Cas2Status>>

  @GetExchange(value = "/cas3/external/referrals/{crn}")
  fun getCas3Referral(@PathVariable crn: String): List<ReferralHistory<TemporaryAccommodationAssessmentStatus>>
}

@Service
open class ApprovedPremisesCachingService(
  private val approvedPremisesClient: ApprovedPremisesClient,
) {
  @Cacheable(ApiCallKeys.GET_CAS1_REFERRAL, key = "#crn")
  open fun getCas1Referral(crn: String) = approvedPremisesClient.getCas1Referral(crn)

  @Cacheable(ApiCallKeys.GET_CAS2_REFERRAL, key = "#crn")
  open fun getCas2Referral(crn: String) = approvedPremisesClient.getCas2Referral(crn)

  @Cacheable(ApiCallKeys.GET_CAS2V2_REFERRAL, key = "#crn")
  open fun getCas2v2Referral(crn: String) = approvedPremisesClient.getCas2v2Referral(crn)

  @Cacheable(ApiCallKeys.GET_CAS3_REFERRAL, key = "#crn")
  open fun getCas3Referral(crn: String) = approvedPremisesClient.getCas3Referral(crn)

  @Cacheable(ApiCallKeys.GET_SUITABLE_CAS1_APPLICATION, key = "#crn", sync = true)
  open fun getSuitableCas1Application(crn: String) = approvedPremisesClient.getSuitableCas1ApplicationInternal(crn)
}
