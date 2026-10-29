package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2ArrivalDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2Arrival

@Service
class EligibilityMutationService(
  private val approvedPremisesCachingService: ApprovedPremisesCachingService,
) {

  private val log = LoggerFactory.getLogger(javaClass)

  fun markCas2Arrival(crn: String, applicationId: String, cas2Arrival: Cas2ArrivalDto, arrivedByUsername: String) {
    log.debug("Marking CAS2 arrival for CRN: {}, applicationId: {}, markedAsArrivedDateTime: {}, arrivedByUsername: {}", crn, applicationId, cas2Arrival.markedAsArrivedDateTime, arrivedByUsername)
    approvedPremisesCachingService.postCas2Arrival(
      crn,
      applicationId,
      Cas2Arrival(
        markedAsArrivedDateTime = cas2Arrival.markedAsArrivedDateTime,
        arrivedByUsername = arrivedByUsername,
      ),
    )
  }
}
