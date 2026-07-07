package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys

interface ApprovedPremisesClient {
  @GetExchange(value = "/cas1/external/cases/{crn}/premises/current")
  fun getCas1CurrentPremises(@PathVariable crn: String): Cas1PremisesSummary

  @GetExchange(value = "/cas3/external/cases/{crn}/premises/current")
  fun getCas3CurrentPremises(@PathVariable crn: String): Cas3PremisesSummary

  @GetExchange(value = "/cas1/external/cases/{crn}/applications/suitable")
  fun getSuitableCas1ApplicationInternal(@PathVariable crn: String): Cas1Application

  @GetExchange(value = "/cas3/external/cases/{crn}/applications/suitable")
  fun getSuitableCas3ApplicationInternal(@PathVariable crn: String): Cas3Application

  @GetExchange(value = "/cas1/external/referrals/{crn}")
  fun getCas1Referral(@PathVariable crn: String): List<Cas1ReferralHistory>

  @GetExchange(value = "/cas3/external/referrals/{crn}")
  fun getCas3Referral(@PathVariable crn: String): List<Cas3ReferralHistory>

  @GetExchange(value = "/cas1/external/url-templates")
  fun getCas1UrlTemplatesInternal(): Cas1UrlTemplates

  @GetExchange(value = "/cas3/external/url-templates")
  fun getCas3UrlTemplatesInternal(): Cas3UrlTemplates
}

@Service
open class ApprovedPremisesCachingService(
  private val approvedPremisesClient: ApprovedPremisesClient,
) {
  @Cacheable(ApiCallKeys.GET_CAS1_REFERRAL, key = "#crn")
  open fun getCas1Referral(crn: String) = approvedPremisesClient.getCas1Referral(crn)

  @Cacheable(ApiCallKeys.GET_CAS3_REFERRAL, key = "#crn")
  open fun getCas3Referral(crn: String) = approvedPremisesClient.getCas3Referral(crn)

  @Cacheable(ApiCallKeys.GET_CAS_1_CURRENT_PREMISES, key = "#crn", sync = true)
  open fun getCas1CurrentPremises(crn: String) = approvedPremisesClient.getCas1CurrentPremises(crn)

  @Cacheable(ApiCallKeys.GET_CAS_3_CURRENT_PREMISES, key = "#crn", sync = true)
  open fun getCas3CurrentPremises(crn: String) = approvedPremisesClient.getCas3CurrentPremises(crn)

  @Cacheable(ApiCallKeys.GET_CAS_1_APPLICATION, key = "#crn", sync = true)
  open fun getSuitableCas1Application(crn: String) = approvedPremisesClient.getSuitableCas1ApplicationInternal(crn)

  @Cacheable(ApiCallKeys.GET_CAS_3_APPLICATION, key = "#crn", sync = true)
  open fun getSuitableCas3Application(crn: String) = approvedPremisesClient.getSuitableCas3ApplicationInternal(crn)

  @Cacheable(ApiCallKeys.GET_CAS_1_URL_TEMPLATES, sync = true)
  open fun getCas1UrlTemplates() = approvedPremisesClient.getCas1UrlTemplatesInternal()

  @Cacheable(ApiCallKeys.GET_CAS_3_URL_TEMPLATES, sync = true)
  open fun getCas3UrlTemplates() = approvedPremisesClient.getCas3UrlTemplatesInternal()
}
