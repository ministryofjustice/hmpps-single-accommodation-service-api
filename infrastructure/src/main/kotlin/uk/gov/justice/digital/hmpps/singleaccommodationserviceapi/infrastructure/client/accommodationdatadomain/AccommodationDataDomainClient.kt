package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain

import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange

interface AccommodationDataDomainClient {
  @GetExchange(value = "/health")
  fun getHealth(): String

  @GetExchange(value = "/cases/{crn}/duty-to-refers")
  fun getDutyToRefer(@PathVariable crn: String): DutyToRefer

  @GetExchange(value = "/cases/{crn}/crs")
  fun getCrs(@PathVariable crn: String): Crs

  @GetExchange(value = "/current-accommodation")
  fun getCurrentAccommodation(): Accommodation

  @GetExchange(value = "/proposed-accommodations")
  fun getProposedAccommodations(): List<Accommodation>
}

@Service
class AccommodationDataDomainCachingService(
  private val accommodationDataDomainClient: AccommodationDataDomainClient,
) {
  fun getHealth() = accommodationDataDomainClient.getHealth()

  fun getDutyToRefer(crn: String) = accommodationDataDomainClient.getDutyToRefer(crn)

  fun getCrs(crn: String) = accommodationDataDomainClient.getCrs(crn)

  fun getCurrentAccommodation() = accommodationDataDomainClient.getCurrentAccommodation()

  fun getProposedAccommodations() = accommodationDataDomainClient.getProposedAccommodations()
}
