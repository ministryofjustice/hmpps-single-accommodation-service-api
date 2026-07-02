package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ProposedAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationStatusRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import java.util.UUID

@Service
class ProposedAccommodationQueryService(
  private val userRepository: UserRepository,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val accommodationTypeRepository: AccommodationTypeRepository,
  private val accommodationStatusRepository: AccommodationStatusRepository,
) {
  fun getProposedAccommodations(crn: String): List<ProposedAccommodationDto> {
    val proposedAccommodations = proposedAccommodationRepository.findAllProposedAccommodationByCrnOrderByCreatedAtDesc(crn)
    return if (proposedAccommodations.isNotEmpty()) {
      val deduplicatedUserIds = proposedAccommodations.mapNotNull { it.createdByUserId }.toSet()
      val accommodationTypeIds = proposedAccommodations.mapNotNull { it.accommodationTypeId }.toSet()
      val accommodationStatusIds = proposedAccommodations.mapNotNull { it.accommodationStatusId }.toSet()

      val createdByUsers = userRepository.findAllById(deduplicatedUserIds)
      val accommodationTypes = accommodationTypeRepository.findAllById(accommodationTypeIds)
      val accommodationStatuses = accommodationStatusRepository.findAllById(accommodationStatusIds)
      proposedAccommodations.map { pa ->
        val createdByUser = createdByUsers.first { it.id == pa.createdByUserId }!!
        val accommodationType = accommodationTypes.firstOrNull { it.id == pa.accommodationTypeId }
        val accommodationStatus = accommodationStatuses.firstOrNull { it.id == pa.accommodationStatusId }

        ProposedAccommodationTransformer.toAccommodationDetail(pa, accommodationType, accommodationStatus, crn, createdByUser.displayName())
      }
    } else {
      emptyList()
    }
  }

  fun getProposedAccommodation(crn: String, id: UUID): ProposedAccommodationDto {
    val proposedAccommodationEntity = proposedAccommodationRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val createdByUser = userRepository.findByIdOrNull(proposedAccommodationEntity.createdByUserId!!)
    val accommodationTypeEntity = proposedAccommodationEntity.accommodationTypeId?.let {
      accommodationTypeRepository.findByIdOrNull(it)
        .orThrowNotFound("accommodationTypeId" to it)
    }
    val accommodationStatusEntity = proposedAccommodationEntity.accommodationStatusId?.let {
      accommodationStatusRepository.findByIdOrNull(it)
        .orThrowNotFound("accommodationStatusId" to it)
    }
    return ProposedAccommodationTransformer.toAccommodationDetail(proposedAccommodationEntity, accommodationTypeEntity, accommodationStatusEntity, crn, createdByUser!!.displayName())
  }
}
