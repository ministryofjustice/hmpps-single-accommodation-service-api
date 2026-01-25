package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import java.util.UUID
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

  fun getProposedAccommodation(crn: String, id: UUID): AccommodationDetail {
    val entity = proposedAccommodationRepository.findByIdAndCrn(id, crn)
      ?: throw NotFoundException("Proposed accommodation with id $id not found for crn $crn")
    val createdByUser = userRepository.findByIdOrNull(entity.createdByUserId!!)
    return ProposedAccommodationTransformer.toAccommodationDetail(entity, createdByUser!!.name)
  }

  fun getProposedAccommodation(id: UUID): AccommodationDetail {
    val entity = proposedAccommodationRepository.findById(id).orElseThrow {
      NotFoundException("Proposed accommodation with id $id not found")
    }
    val createdByUser = userRepository.findByIdOrNull(entity.createdByUserId!!)
    return ProposedAccommodationTransformer.toAccommodationDetail(entity, createdByUser!!.name)
  }
}
