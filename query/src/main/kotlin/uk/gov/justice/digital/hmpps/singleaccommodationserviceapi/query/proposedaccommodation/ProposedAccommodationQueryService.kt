package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository

@Service
class ProposedAccommodationQueryService(
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
) {
  fun getProposedAccommodations(crn: String): List<AccommodationDetail> {
    val entities = proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn)
    return ProposedAccommodationTransformer.toAccommodationDetails(entities)
  }
}
