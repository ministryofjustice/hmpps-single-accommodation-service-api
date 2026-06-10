package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain

import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.service.annotation.GetExchange

interface AccommodationDataDomainClient {
  @GetExchange(value = "/health")
  fun getHealth(): String
}

@Retryable(interceptor = "retryInterceptor")
@Service
class AccommodationDataDomainCachingService(
  private val accommodationDataDomainClient: AccommodationDataDomainClient,
) {
  fun getHealth() = accommodationDataDomainClient.getHealth()
}
