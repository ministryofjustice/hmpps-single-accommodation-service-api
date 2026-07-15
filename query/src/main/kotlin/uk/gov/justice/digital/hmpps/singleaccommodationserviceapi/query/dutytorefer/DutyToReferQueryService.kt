package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FieldChange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DtrStatus.ACCEPTED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DtrStatus.NOT_ACCEPTED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DtrStatus.WITHDRAWN
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isDtrExpired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto
import java.time.Clock
import java.util.UUID

private const val LOCAL_AUTHORITY_AREA_NAME = "localAuthorityAreaName"
private val HISTORY_STATUSES = listOf(ACCEPTED, NOT_ACCEPTED, WITHDRAWN)

@Service
class DutyToReferQueryService(
  private val dutyToReferRepository: DutyToReferRepository,
  private val userRepository: UserRepository,
  private val localAuthorityAreaRepository: LocalAuthorityAreaRepository,
  private val caseRepository: CaseRepository,
  private val auditService: AuditService,
  private val clock: Clock,
) {
  fun getDutyToReferHistory(crn: String): List<DutyToReferDto> {
    val caseEntity = caseRepository.findByCrn(crn) ?: return emptyList()
    return getDutyToReferHistory(caseEntity, crn)
  }

  private fun isActiveDtr(dtr: DutyToReferEntity, clock: Clock): Boolean = dtr.status != WITHDRAWN && !isDtrExpired(dtr.submissionDate, clock)

  fun getDutyToReferHistory(caseEntity: CaseEntity, crn: String): List<DutyToReferDto> {
    val activeDtrId = dutyToReferRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseEntity.id)
      ?.takeIf { isActiveDtr(it, clock) }
      ?.id

    val dtrEntities = dutyToReferRepository
      .findByCaseIdAndStatusInOrderByCreatedAtDesc(caseEntity.id, HISTORY_STATUSES)
      .filter { it.id != activeDtrId }
    if (dtrEntities.isEmpty()) return emptyList()

    val createdByUserIds = dtrEntities.mapNotNull { it.createdByUserId }.toSet()
    val localAuthorityAreaIds = dtrEntities.map { it.localAuthorityAreaId }.toSet()

    val users = userRepository.findAllById(createdByUserIds).associateBy { it.id }
    val localAuthorities = localAuthorityAreaRepository.findAllById(localAuthorityAreaIds).associateBy { it.id }

    return dtrEntities.map { dtrEntity ->
      val createdByUser = users[dtrEntity.createdByUserId]
      val localAuthorityArea = localAuthorities[dtrEntity.localAuthorityAreaId]
      DutyToReferTransformer.toDutyToReferDto(dtrEntity, crn, createdByUser!!, localAuthorityArea!!.name)
    }
  }

  fun getDutyToRefer(caseEntity: CaseEntity, crn: String): DutyToReferDto? = dutyToReferRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseEntity.id)
    ?.let { dtrEntity ->
      val createdByUser = userRepository.findByIdOrNull(dtrEntity.createdByUserId!!)
      val localAuthorityArea = localAuthorityAreaRepository.findByIdOrNull(dtrEntity.localAuthorityAreaId)
      DutyToReferTransformer.toDutyToReferDto(dtrEntity, crn, createdByUser!!.displayName(), localAuthorityArea!!.name)
    }

  fun getDutyToRefer(crn: String, id: UUID): DutyToReferDto {
    val entity = dutyToReferRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val createdByUser = userRepository.findByIdOrNull(entity.createdByUserId!!)
    val localAuthorityArea = localAuthorityAreaRepository.findByIdOrNull(entity.localAuthorityAreaId)

    return DutyToReferTransformer.toDutyToReferDto(entity, crn, createdByUser!!.displayName(), localAuthorityArea!!.name)
  }

  fun getDutyToRefer(id: UUID): DutyToReferDto {
    val dtrEntity = dutyToReferRepository.findByIdOrNull(id).orThrowNotFound("id" to id)
    val caseEntity = caseRepository.findWithIdentifiersById(dtrEntity.caseId).orThrowNotFound("id" to id)
    val createdByUser = userRepository.findByIdOrNull(dtrEntity.createdByUserId!!)
    val localAuthorityArea = localAuthorityAreaRepository.findByIdOrNull(dtrEntity.localAuthorityAreaId)

    return DutyToReferTransformer.toDutyToReferDto(
      dtrEntity,
      crn = caseEntity.latestCrn(),
      createdByUser!!.displayName(),
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
      val laIdChange = record.changes.firstOrNull { it.field == "localAuthorityAreaId" && it.oldValue != null }
      if (laIdChange != null) {
        effectiveLaId = laIdChange.oldValue?.let(UUID::fromString)
      }
      record to atCommit
    }

    val laNameById = localAuthorityAreaRepository
      .findAllById(laIdPerRecord.mapNotNull { it.second }.toSet())
      .associate { it.id to it.name }

    return laIdPerRecord.map { (record, laId) ->
      createDutyToReferTimelineRecordWithLAName(record, laId?.let(laNameById::get))
    }
  }

  private fun createDutyToReferTimelineRecordWithLAName(record: AuditRecordDto, localAuthorityAreaName: String?): AuditRecordDto {
    if (localAuthorityAreaName == null) return record
    return record.copy(extraInformation = record.extraInformation.orEmpty() + (LOCAL_AUTHORITY_AREA_NAME to localAuthorityAreaName))
  }

  private fun getDutyToReferNotesTimeline(dtrEntity: DutyToReferEntity): List<AuditRecordDto> {
    val createdByUserIds = dtrEntity.notes.mapNotNull { it.createdByUserId }.toSet()
    val createdByUsers = userRepository.findAllById(createdByUserIds).associateBy { it.id }
    return dtrEntity.notes.map {
      val createdByUser = createdByUsers[it.createdByUserId]
      AuditRecordDto(
        type = AuditRecordType.NOTE,
        author = createdByUser!!.displayName(),
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
