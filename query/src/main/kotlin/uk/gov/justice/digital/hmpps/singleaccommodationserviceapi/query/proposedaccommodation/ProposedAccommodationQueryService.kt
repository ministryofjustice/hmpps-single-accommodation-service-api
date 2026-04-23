package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CreateFieldChangeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ProposedAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import java.util.UUID
import kotlin.collections.get

@Service
class ProposedAccommodationQueryService(
  private val auditService: AuditService,
  private val userRepository: UserRepository,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val caseRepository: CaseRepository,
) {
  fun getProposedAccommodations(crn: String): List<ProposedAccommodationDto> = proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn).map {
    val createdByUser = userRepository.findByIdOrNull(it.createdByUserId!!)
      .orThrowNotFound("id" to it.createdByUserId!!)
    ProposedAccommodationTransformer.toAccommodationDetail(it, crn, createdByUser.name)
  }

  fun getProposedAccommodation(crn: String, id: UUID): ProposedAccommodationDto {
    val entity = proposedAccommodationRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val createdByUser = userRepository.findByIdOrNull(entity.createdByUserId!!)
    return ProposedAccommodationTransformer.toAccommodationDetail(entity, crn, createdByUser!!.name)
  }

  fun getProposedAccommodation(id: UUID): ProposedAccommodationDto {
    val entity = proposedAccommodationRepository.findByIdOrNull(id).orThrowNotFound("id" to id)
    val case = caseRepository.findByIdOrNull(entity.caseId).orThrowNotFound("id" to entity.id)
    val createdByUser = userRepository.findByIdOrNull(entity.createdByUserId!!)
    return ProposedAccommodationTransformer.toAccommodationDetail(entity, case.latestCrn(), createdByUser!!.name)
  }

  fun getProposedAccommodationTimeline(id: UUID, crn: String): List<AuditRecordDto> {
    val proposedAccommodationEntity = proposedAccommodationRepository.findByIdAndCrnWithNotes(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val proposedAccommodationAuditHistory = auditService.fullAuditHistory(id = proposedAccommodationEntity.id, ProposedAccommodationEntity::class.java)
    if (proposedAccommodationEntity.notes.isNotEmpty()) {
      val proposedAccommodationNotesAuditHistory = getProposedAccommodationNotesAuditHistory(proposedAccommodationEntity)
      return (proposedAccommodationAuditHistory + proposedAccommodationNotesAuditHistory)
        .sortedByDescending { it.commitDate }
    }
    return proposedAccommodationAuditHistory
  }

  private fun getProposedAccommodationNotesAuditHistory(proposedAccommodationEntity: ProposedAccommodationEntity): List<AuditRecordDto> {
    val createdByUserIds = proposedAccommodationEntity.notes.mapNotNull { it.createdByUserId }.toSet()
    val createdByUsers = userRepository.findAllById(createdByUserIds).associateBy { it.id }
    return proposedAccommodationEntity.notes.map {
      val createdByUser = createdByUsers[it.createdByUserId]
      AuditRecordDto(
        type = AuditRecordType.NOTE,
        author = createdByUser!!.name,
        commitDate = it.createdAt!!,
        changes = listOf(
          CreateFieldChangeDto(
            field = "note",
            value = it.note,
          ),
        ),
      )
    }
  }
}
