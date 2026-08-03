package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain

import org.springframework.stereotype.Service
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config.RestClientRetry

interface AccommodationDataDomainClient {
  @GetExchange(value = "/health")
  fun getHealth(): String
}

@RestClientRetry
@Service
class AccommodationDataDomainCachingService(
  private val accommodationDataDomainClient: AccommodationDataDomainClient,
) {
  fun getHealth() = accommodationDataDomainClient.getHealth()
}
