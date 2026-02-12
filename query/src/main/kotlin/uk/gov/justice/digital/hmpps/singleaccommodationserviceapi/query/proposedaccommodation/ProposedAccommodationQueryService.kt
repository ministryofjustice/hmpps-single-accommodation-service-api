package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.domain.exceptions.NotFoundException
import java.util.UUID

@Service
class ProposedAccommodationQueryService(
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
) {
  fun getProposedAccommodations(crn: String): List<AccommodationDetail> {
    val entities = proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn)
    return ProposedAccommodationTransformer.toAccommodationDetails(entities)
  }

  fun getProposedAccommodation(crn: String, id: UUID): AccommodationDetail {
    val entity = proposedAccommodationRepository.findByIdAndCrn(id, crn)
      ?: throw NotFoundException("Proposed accommodation with id $id not found for crn $crn")
    return ProposedAccommodationTransformer.toAccommodationDetail(entity)
  }
}
