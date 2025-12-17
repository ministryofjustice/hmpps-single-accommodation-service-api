package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.accommodation.AccommodationResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_ACCOMMODATION_RESPONSE
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CAS1_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CAS2V2_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CAS2_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CAS3_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_SUITABLE_CAS1_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model.TemporaryAccommodationAssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock.mockAccommodationResponse

interface ApprovedPremisesClient {

  @GetExchange(value = "/info")
  fun getInfo(): String

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
class ApprovedPremisesCachingService(
  private val approvedPremisesClient: ApprovedPremisesClient,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @Cacheable(GET_CAS1_REFERRAL, key = "#crn")
  fun getCas1Referral(crn: String) = approvedPremisesClient.getCas1Referral(crn)

  @Cacheable(GET_CAS2_REFERRAL, key = "#crn")
  fun getCas2Referral(crn: String) = approvedPremisesClient.getCas2Referral(crn)

  @Cacheable(GET_CAS2V2_REFERRAL, key = "#crn")
  fun getCas2v2Referral(crn: String) = approvedPremisesClient.getCas2v2Referral(crn)

  @Cacheable(GET_CAS3_REFERRAL, key = "#crn")
  fun getCas3Referral(crn: String) = approvedPremisesClient.getCas3Referral(crn)

  @Cacheable(GET_ACCOMMODATION_RESPONSE, key = "#crn", sync = true)
  fun getAccommodationResponse(crn: String): AccommodationResponse {
    log.warn("Mocking accommodation result for crn: $crn")
    return mockAccommodationResponse(crn)
  }

  @Cacheable(GET_SUITABLE_CAS1_APPLICATION, key = "#crn", sync = true)
  fun getSuitableCas1Application(crn: String) = approvedPremisesClient.getSuitableCas1ApplicationInternal(crn)
}
