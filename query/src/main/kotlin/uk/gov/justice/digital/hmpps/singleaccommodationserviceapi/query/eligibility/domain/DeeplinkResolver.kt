package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService

@Component
class DeeplinkResolver(
  private val approvedPremisesCachingService: ApprovedPremisesCachingService,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun resolve(result: ServiceResultNew, crn: String, applicationUrl: String? = null): ServiceResultNew {
    val url = when (result.serviceStatus.link?.type) {
      LinkType.CAS1_START_APPLICATION -> cas1UrlTemplates()?.cas1ApplicationStart
      LinkType.CAS1_VIEW_APPLICATION -> applicationUrl

      LinkType.CAS2_START_APPLICATION -> cas2UrlTemplates()?.cas2ApplicationStart?.replace("#crn", crn)
      LinkType.CAS2_VIEW_APPLICATION -> applicationUrl

      LinkType.CAS3_START_REFERRAL -> cas3UrlTemplates()?.cas3ReferralStart
      LinkType.CAS3_VIEW_REFERRAL -> applicationUrl
      null -> return result
    }
    return result.copy(url = url)
  }

  private fun cas1UrlTemplates() = runCatching { approvedPremisesCachingService.getCas1UrlTemplates() }
    .onFailure { log.warn("Failed to fetch CAS1 url templates", it) }
    .getOrNull()

  private fun cas2UrlTemplates() = runCatching { approvedPremisesCachingService.getCas2UrlTemplates() }
    .onFailure { log.warn("Failed to fetch CAS2 url templates", it) }
    .getOrNull()

  private fun cas3UrlTemplates() = runCatching { approvedPremisesCachingService.getCas3UrlTemplates() }
    .onFailure { log.warn("Failed to fetch CAS3 url templates", it) }
    .getOrNull()
}
