package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CreateFieldChangeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpdateFieldChangeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.withExtraInformation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferAuditKeys.LOCAL_AUTHORITY_AREA_NAME
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto
import java.util.UUID

@Service
class DutyToReferQueryService(
  private val dutyToReferRepository: DutyToReferRepository,
  private val userRepository: UserRepository,
  private val localAuthorityAreaRepository: LocalAuthorityAreaRepository,
  private val caseRepository: CaseRepository,
  private val auditService: AuditService,
) {
  fun getDutyToRefer(crn: String): DutyToReferDto {
    val caseEntity = caseRepository.findByCrn(crn).orThrowNotFound("crn" to crn)
    val dtrEntity = dutyToReferRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseEntity.id)
      ?: return DutyToReferTransformer.toNotStartedDto(caseEntity.id, crn)

    val createdByUser = userRepository.findByIdOrNull(dtrEntity.createdByUserId!!)
    val localAuthorityArea = localAuthorityAreaRepository.findByIdOrNull(dtrEntity.localAuthorityAreaId)

    return DutyToReferTransformer.toDutyToReferDto(dtrEntity, crn, createdByUser!!.name, localAuthorityArea!!.name)
  }

  fun getDutyToRefer(crn: String, id: UUID): DutyToReferDto {
    val entity = dutyToReferRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val createdByUser = userRepository.findByIdOrNull(entity.createdByUserId!!)
    val localAuthorityArea = localAuthorityAreaRepository.findByIdOrNull(entity.localAuthorityAreaId)

    return DutyToReferTransformer.toDutyToReferDto(entity, crn, createdByUser!!.name, localAuthorityArea!!.name)
  }

  fun getDutyToRefer(id: UUID): DutyToReferDto {
    val dtrEntity = dutyToReferRepository.findByIdOrNull(id).orThrowNotFound("id" to id)
    val caseEntity = caseRepository.findByIdOrNull(dtrEntity.caseId).orThrowNotFound("id" to id)
    val createdByUser = userRepository.findByIdOrNull(dtrEntity.createdByUserId!!)
    val localAuthorityArea = localAuthorityAreaRepository.findByIdOrNull(dtrEntity.localAuthorityAreaId)

    return DutyToReferTransformer.toDutyToReferDto(
      dtrEntity,
      crn = caseEntity.latestCrn(),
      createdByUser!!.name,
      localAuthorityArea!!.name,
    )
  }

  fun getDutyToReferTimeline(id: UUID, crn: String): ApiResponseDto<List<AuditRecordDto>> {
    val dtrEntity = dutyToReferRepository.findByIdAndCrnWithNotes(id, crn).orThrowNotFound("id" to id, "crn" to crn)

    val auditHistory = auditService.fullAuditHistory(dtrEntity.id, DutyToReferEntity::class.java)
    val dtrAuditTimelineRecords = getDutyToReferTimelineWithLocalAuthorityNames(auditHistory, dtrEntity.localAuthorityAreaId)

    val noteTimelineRecords =
      if (dtrEntity.notes.isNotEmpty()) getDutyToReferNotesTimeline(dtrEntity) else emptyList()

    val timelineRecords = (dtrAuditTimelineRecords + noteTimelineRecords).sortedByDescending { it.commitDate }

    return toApiResponseDto(
      data = timelineRecords,
    )
  }

  private fun getDutyToReferTimelineWithLocalAuthorityNames(
    auditHistory: List<AuditRecordDto>,
    currentLocalAuthorityAreaId: UUID,
  ): List<AuditRecordDto> {
    val sorted = auditHistory.sortedByDescending { it.commitDate }
    var effectiveLaId: UUID? = currentLocalAuthorityAreaId

    val laIdPerRecord = sorted.map { record ->
      val atCommit = effectiveLaId
      val laIdChange = record.changes.firstOrNull { it.field == "localAuthorityAreaId" } as? UpdateFieldChangeDto
      if (laIdChange != null) {
        effectiveLaId = laIdChange.oldValue?.let(UUID::fromString)
      }
      record to atCommit
    }

    val laNameById = localAuthorityAreaRepository
      .findAllById(laIdPerRecord.mapNotNull { it.second }.toSet())
      .associate { it.id to it.name }

    return laIdPerRecord.map { (record, laId) ->
      record.withExtraInformation(LOCAL_AUTHORITY_AREA_NAME to laId?.let(laNameById::get))
    }
  }

  private fun getDutyToReferNotesTimeline(dtrEntity: DutyToReferEntity): List<AuditRecordDto> {
    val createdByUserIds = dtrEntity.notes.mapNotNull { it.createdByUserId }.toSet()
    val createdByUsers = userRepository.findAllById(createdByUserIds).associateBy { it.id }
    return dtrEntity.notes.map {
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
