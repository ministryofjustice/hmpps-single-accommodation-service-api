package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CreateFieldChangeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FieldChange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpdateFieldChangeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import java.util.UUID
import kotlin.collections.get

@Service
class ProposedAccommodationTimelineService(
  private val auditService: AuditService,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val accommodationTypeRepository: AccommodationTypeRepository,
  private val userRepository: UserRepository,
) {
  fun getProposedAccommodationTimeline(id: UUID, crn: String): List<AuditRecordDto> {
    val proposedAccommodationEntity = proposedAccommodationRepository.findByIdAndCrnWithNotes(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val proposedAccommodationAuditHistory = auditService.fullAuditHistory(id = proposedAccommodationEntity.id, ProposedAccommodationEntity::class.java)
    val accommodationTypes = accommodationTypeRepository.findAll()
    val transformedAuditHistory = replaceAccommodationTypeIdAuditRecord(proposedAccommodationAuditHistory, accommodationTypes)
    if (proposedAccommodationEntity.notes.isNotEmpty()) {
      val proposedAccommodationNotesAuditHistory = getProposedAccommodationNotesAuditHistory(proposedAccommodationEntity)
      return (transformedAuditHistory + proposedAccommodationNotesAuditHistory)
        .sortedByDescending { it.commitDate }
    }
    return transformedAuditHistory
  }

  private fun replaceAccommodationTypeIdAuditRecord(
    proposedAccommodationAuditHistory: List<AuditRecordDto>,
    accommodationTypes: List<AccommodationTypeEntity>,
  ): List<AuditRecordDto> {
    val accommodationTypeLookup = accommodationTypes.associateBy { it.id }
    return proposedAccommodationAuditHistory.map { auditRecord ->
      auditRecord.copy(
        changes = auditRecord.changes.map { change ->
          replaceAccommodationTypeFieldChange(change, accommodationTypeLookup)
        },
      )
    }
  }

  private fun replaceAccommodationTypeFieldChange(
    change: FieldChange,
    accommodationTypes: Map<UUID, AccommodationTypeEntity>,
  ): FieldChange {
    if (change.field != "accommodationTypeId") {
      return change
    }
    return when (change) {
      is CreateFieldChangeDto -> {
        val accommodationType = accommodationTypes.getValue(UUID.fromString(change.value))
        change.copy(
          field = "accommodationTypeDescription",
          value = accommodationType.name,
        )
      }
      is UpdateFieldChangeDto -> {
        val newAccommodationType = accommodationTypes.getValue(UUID.fromString(change.value))
        val oldAccommodationType = accommodationTypes.getValue(UUID.fromString(change.oldValue))
        change.copy(
          field = "accommodationTypeDescription",
          value = newAccommodationType.name,
          oldValue = oldAccommodationType.name,
        )
      }
      else -> change
    }
  }

  private fun getProposedAccommodationNotesAuditHistory(proposedAccommodationEntity: ProposedAccommodationEntity): List<AuditRecordDto> {
    val createdByUserIds = proposedAccommodationEntity.notes.mapNotNull { it.createdByUserId }.toSet()
    val createdByUsers = userRepository.findAllById(createdByUserIds).associateBy { it.id }
    return proposedAccommodationEntity.notes.map {
      val createdByUser = createdByUsers[it.createdByUserId]
      AuditRecordDto(
        type = AuditRecordType.NOTE,
        author = createdByUser!!.displayName(),
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
