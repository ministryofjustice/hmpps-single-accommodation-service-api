package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.PostExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.getOrNullWhenNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config.WebClientRetry

interface ApprovedPremisesClient {
  @WebClientRetry
  @GetExchange(value = "/cas1/external/cases/{crn}/residence")
  fun getCas1Residence(@PathVariable crn: String): Cas1PlacementV2?

  @WebClientRetry
  @GetExchange(value = "/cas3/external/cases/{crn}/premises/current")
  fun getCas3CurrentPremises(@PathVariable crn: String): Cas3BookingPremises?

  @WebClientRetry
  @GetExchange(value = "/cas1/external/cases/{crn}/applications/current")
  fun getCurrentCas1ApplicationInternal(@PathVariable crn: String): Cas1ApplicationV2

  @WebClientRetry
  @GetExchange(value = "/cas2/external/cases/{crn}/applications/current")
  fun getCas2CurrentApplicationInternal(@PathVariable crn: String): Cas2Application?

  @WebClientRetry
  @GetExchange(value = "/cas3/external/cases/{crn}/applications/current")
  fun getCas3CurrentApplicationInternal(@PathVariable crn: String): Cas3Application?

  @WebClientRetry
  @GetExchange(value = "/cas1/external/referrals/{crn}")
  fun getCas1Referral(@PathVariable crn: String): List<Cas1ReferralHistory>

  @WebClientRetry
  @GetExchange(value = "/cas2/external/referrals/{crn}")
  fun getCas2ReferralHistory(@PathVariable crn: String): List<Cas2ReferralHistory>

  @WebClientRetry
  @GetExchange(value = "/cas3/external/referrals/{crn}")
  fun getCas3ReferralHistory(@PathVariable crn: String): List<Cas3ReferralHistory>

  @WebClientRetry
  @GetExchange(value = "/cas1/external/url-templates")
  fun getCas1UrlTemplatesInternal(): Cas1UrlTemplates

  @WebClientRetry
  @GetExchange(value = "/cas2/external/url-templates")
  fun getCas2UrlTemplatesInternal(): Cas2UrlTemplates

  @WebClientRetry
  @GetExchange(value = "/cas3/external/url-templates")
  fun getCas3UrlTemplatesInternal(): Cas3UrlTemplates

  @PostExchange(value = "/cas2/external/cases/{crn}/applications/{applicationId}/arrival")
  fun postCas2Arrival(@PathVariable crn: String, @PathVariable applicationId: String, @RequestBody cas2Arrival: Cas2Arrival)
}

@Service
class ApprovedPremisesCachingService(
  private val approvedPremisesClient: ApprovedPremisesClient,
) {
  @Cacheable(ApiCallKeys.GET_CAS1_REFERRAL)
  fun getCas1ReferralHistory(crn: String) = getOrNullWhenNotFound { approvedPremisesClient.getCas1Referral(crn) }

  @Cacheable(ApiCallKeys.GET_CAS2_REFERRAL)
  fun getCas2ReferralHistory(crn: String) = getOrNullWhenNotFound { approvedPremisesClient.getCas2ReferralHistory(crn) }

  @Cacheable(ApiCallKeys.GET_CAS3_REFERRAL)
  fun getCas3ReferralHistory(crn: String) = getOrNullWhenNotFound { approvedPremisesClient.getCas3ReferralHistory(crn) }

  @Cacheable(ApiCallKeys.GET_CAS_1_RESIDENCE, sync = true)
  fun getCas1Residence(crn: String): Cas1PlacementV2? = approvedPremisesClient.getCas1Residence(crn)

  @Cacheable(ApiCallKeys.GET_CAS_3_CURRENT_PREMISES, sync = true)
  fun getCas3CurrentPremises(crn: String): Cas3BookingPremises? = approvedPremisesClient.getCas3CurrentPremises(crn)

  @Cacheable(ApiCallKeys.GET_CAS_1_CURRENT_APPLICATION, sync = true)
  fun getCurrentCas1Application(crn: String): Cas1ApplicationV2? = approvedPremisesClient.getCurrentCas1ApplicationInternal(crn)

  @Cacheable(ApiCallKeys.GET_CAS_2_CURRENT_APPLICATION, sync = true)
  fun getCas2CurrentApplication(crn: String): Cas2Application? = approvedPremisesClient.getCas2CurrentApplicationInternal(crn)

  @Cacheable(ApiCallKeys.GET_CAS_3_CURRENT_APPLICATION, sync = true)
  fun getCas3CurrentApplication(crn: String): Cas3Application? = approvedPremisesClient.getCas3CurrentApplicationInternal(crn)

  @Cacheable(ApiCallKeys.GET_CAS_1_URL_TEMPLATES, sync = true)
  fun getCas1UrlTemplates() = approvedPremisesClient.getCas1UrlTemplatesInternal()

  @Cacheable(ApiCallKeys.GET_CAS_2_URL_TEMPLATES, sync = true)
  fun getCas2UrlTemplates() = approvedPremisesClient.getCas2UrlTemplatesInternal()

  @Cacheable(ApiCallKeys.GET_CAS_3_URL_TEMPLATES, sync = true)
  fun getCas3UrlTemplates() = approvedPremisesClient.getCas3UrlTemplatesInternal()

  fun postCas2Arrival(crn: String, applicationId: String, cas2Arrival: Cas2Arrival) {
    approvedPremisesClient.postCas2Arrival(crn, applicationId, cas2Arrival)
  }
}
