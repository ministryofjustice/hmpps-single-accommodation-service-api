package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain

import org.springframework.stereotype.Service
import org.springframework.web.service.annotation.GetExchange

interface AccommodationDataDomainClient {
  @GetExchange(value = "/info")
  fun getInfo(): String
}

@Service
class AccommodationDataDomainCachingService(
  private val accommodationDataDomainClient: AccommodationDataDomainClient,
) {
  fun getInfo() = accommodationDataDomainClient.getInfo()
}
