package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService

@Component
class DeeplinkResolver(
  private val approvedPremisesCachingService: ApprovedPremisesCachingService,
) {
  fun resolve(result: ServiceResult, data: DomainData): ServiceResult {
    val url = when (result.linkType) {
      LinkType.CAS1_START_APPLICATION -> approvedPremisesCachingService.getCas1UrlTemplates().cas1ApplicationStart
      LinkType.CAS1_VIEW_APPLICATION -> data.cas1Application?.uiUrl
      LinkType.CAS3_START_REFERRAL -> approvedPremisesCachingService.getCas3UrlTemplates().cas3ReferralStart
      LinkType.CAS3_VIEW_REFERRAL -> data.cas3Application?.uiUrl
      null -> return result
    }
    return result.copy(url = url)
  }
}
