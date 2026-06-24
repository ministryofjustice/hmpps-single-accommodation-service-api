package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult

@Component
class DeeplinkResolver(
  @Value($$"${service.approved-premises-ui.base-url}") private val cas1BaseUrl: String,
  @Value($$"${service.temporary-accommodation-ui.base-url}") private val cas3BaseUrl: String,
) {
  fun resolve(result: ServiceResult, data: DomainData): ServiceResult {
    val url = when (result.linkType) {
      LinkType.CAS1_START_APPLICATION -> "$cas1BaseUrl/applications/start"
      LinkType.CAS1_VIEW_APPLICATION -> data.cas1Application?.id?.let { "$cas1BaseUrl/applications/$it" }
      LinkType.CAS3_START_REFERRAL -> "$cas3BaseUrl/referrals/start"
      LinkType.CAS3_VIEW_REFERRAL -> data.cas3Application?.id?.let { "$cas3BaseUrl/referrals/$it" }
      null -> return result
    }
    return result.copy(url = url)
  }
}
