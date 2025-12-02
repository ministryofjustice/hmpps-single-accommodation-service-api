package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock.mockAccommodationStatus

interface ApprovedPremisesClient {

  @GetExchange(value = "/info")
  fun getInfo(): String

  @GetExchange(value = "/cas1/external/suitable-applications/{crn}")
  fun getSuitableCas1ApplicationInternal(@PathVariable crn: String): Cas1Application
}

@Service
class ApprovedPremisesCachingService(
  val approvedPremisesClient: ApprovedPremisesClient,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @Cacheable("getAccommodationStatus", key = "#crn", sync = true)
  fun getAccommodationStatus(crn: String): AccommodationStatus {
    log.warn("Mocking accommodation result for crn: $crn")
    return mockAccommodationStatus
  }

  @Cacheable("getSuitableCas1ApplicationByCrn", key = "#crn", sync = true)
  fun getSuitableCas1Application(crn: String) = approvedPremisesClient.getSuitableCas1ApplicationInternal(crn)
}
