package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData

@Component
class Cas1DeeplinkResolver(
  @Value($$"${service.approved-premises-ui.base-url}") private val baseUrl: String,
) {
  fun resolve(result: ServiceResult, data: DomainData): ServiceResult {
    val url = when (result.linkType) {
      LinkType.CAS1_START_APPLICATION -> "$baseUrl/applications/start"
      LinkType.CAS1_VIEW_APPLICATION -> data.cas1Application?.id?.let { "$baseUrl/applications/$it" }
      else -> return result
    }
    return result.copy(url = url)
  }
}
