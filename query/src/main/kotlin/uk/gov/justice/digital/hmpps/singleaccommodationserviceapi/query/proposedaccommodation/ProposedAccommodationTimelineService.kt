package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FieldChange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.toAssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.Username
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
    val deliusSyncUser = userRepository.findByUsernameAndAuthSource(
      username = Username("DELIUS_SYNC_USER"),
      authSource = AuthSource.DELIUS,
    )!!
    val proposedAccommodationEntity = proposedAccommodationRepository.findByIdAndCrnWithNotes(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val proposedAccommodationAuditHistory = auditService.fullAuditHistory(id = proposedAccommodationEntity.id, ProposedAccommodationEntity::class.java)
    val accommodationTypes = accommodationTypeRepository.findAll()
    val auditHistoryWithAccommodationTypesTransformed = replaceAccommodationTypeIdAuditRecord(proposedAccommodationAuditHistory, accommodationTypes)
    if (proposedAccommodationEntity.notes.isNotEmpty()) {
      val proposedAccommodationNotesAuditHistory = getProposedAccommodationNotesAuditHistory(proposedAccommodationEntity)
      val fullHistory = (auditHistoryWithAccommodationTypesTransformed + proposedAccommodationNotesAuditHistory)
        .sortedByDescending { it.commitDate }
      return nullifyCommitAndUpdateDatesForDeliusSyncUserAudits(
        proposedAccommodationAuditHistory = fullHistory,
        deliusSyncUser,
      )
    }
    return nullifyCommitAndUpdateDatesForDeliusSyncUserAudits(
      proposedAccommodationAuditHistory = auditHistoryWithAccommodationTypesTransformed,
      deliusSyncUser,
    )
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

  private fun nullifyCommitAndUpdateDatesForDeliusSyncUserAudits(
    proposedAccommodationAuditHistory: List<AuditRecordDto>,
    deliusSyncUser: UserEntity,
  ): List<AuditRecordDto> = proposedAccommodationAuditHistory.map { auditRecord ->
    // TODO - switch to auditRecord.authorDetails?.username == deliusSyncUser.username when .author is removed
    if (auditRecord.author == deliusSyncUser.displayName()) {
      auditRecord.copy(
        commitDate = null,
      )
    } else {
      auditRecord
    }
  }

  private fun replaceAccommodationTypeFieldChange(
    change: FieldChange,
    accommodationTypes: Map<UUID, AccommodationTypeEntity>,
  ): FieldChange {
    if (change.field != "accommodationTypeId") {
      return change
    }
    return change.copy(
      field = "accommodationTypeDescription",
      value = accommodationTypes.getValue(UUID.fromString(change.value)).name,
      oldValue = change.oldValue?.let { accommodationTypes.getValue(UUID.fromString(it)).name },
    )
  }

  private fun getProposedAccommodationNotesAuditHistory(proposedAccommodationEntity: ProposedAccommodationEntity): List<AuditRecordDto> {
    val createdByUserIds = proposedAccommodationEntity.notes.mapNotNull { it.createdByUserId }.toSet()
    val createdByUsers = userRepository.findAllById(createdByUserIds).associateBy { it.id }
    return proposedAccommodationEntity.notes.map {
      val createdByUser = createdByUsers[it.createdByUserId]
      AuditRecordDto(
        type = AuditRecordType.NOTE,
        author = createdByUser!!.displayName(),
        authorDetails = createdByUser.toAssignedToDto(),
        commitDate = it.createdAt!!,
        changes = listOf(
          FieldChange(
            field = "note",
            value = it.note,
          ),
        ),
      )
    }
  }
}
