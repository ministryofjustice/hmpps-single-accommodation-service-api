package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationdatadomain

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain.AccommodationDataDomainCachingService

@Service
class AccommodationDataDomainService(private val accommodationDataDomainCachingService: AccommodationDataDomainCachingService) {

  fun getHealth(): String = accommodationDataDomainCachingService.getHealth()
}
