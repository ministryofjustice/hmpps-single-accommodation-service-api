package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository

@Service
class ProposedAccommodationQueryService(
  private val userRepository: UserRepository,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
) {
  fun getProposedAccommodations(crn: String): List<AccommodationDetail> = proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn).map {
    val createdByUser = userRepository.findById(it.createdByUserId!!)
    ProposedAccommodationTransformer.toAccommodationDetail(it, createdByUser.get().name)
  }
}
