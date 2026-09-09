package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.outofregionreferral

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FieldChange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutOfRegionReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OorStatus.ACCEPTED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OorStatus.NOT_ACCEPTED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutOfRegionReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.toAssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutOfRegionReferralRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isDtrExpired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto
import java.time.Clock
import java.util.UUID

private val HISTORY_STATUSES = listOf(ACCEPTED, NOT_ACCEPTED)

@Service
class OutOfRegionReferralQueryService(
  private val outOfRegionReferralRepository: OutOfRegionReferralRepository,
  private val userRepository: UserRepository,
  private val caseRepository: CaseRepository,
  private val auditService: AuditService,
  private val clock: Clock,
) {
  fun getOutOfRegionReferralHistory(crn: String): List<OutOfRegionReferralDto> {
    val caseEntity = caseRepository.findByCrn(crn) ?: return emptyList()
    return getOutOfRegionReferralHistory(caseEntity, crn)
  }

  private fun isActiveOor(oor: OutOfRegionReferralEntity): Boolean = !isDtrExpired(oor.submissionDate, clock)

  private fun getActiveOorId(caseId: UUID): UUID? = outOfRegionReferralRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId)
    ?.takeIf { isActiveOor(it) }
    ?.id

  fun getOutOfRegionReferralHistory(caseEntity: CaseEntity, crn: String): List<OutOfRegionReferralDto> {
    val activeOorId = getActiveOorId(caseEntity.id)

    val oorEntities = outOfRegionReferralRepository
      .findByCaseIdAndStatusInOrderByCreatedAtDesc(caseEntity.id, HISTORY_STATUSES)
      .filter { it.id != activeOorId }
    if (oorEntities.isEmpty()) return emptyList()

    val createdByUserIds = oorEntities.mapNotNull { it.createdByUserId }.toSet()
    val users = userRepository.findAllById(createdByUserIds).associateBy { it.id }

    return oorEntities.map { oorEntity ->
      val createdByUser = users[oorEntity.createdByUserId]
      OutOfRegionReferralTransformer.toOutOfRegionReferralDto(oorEntity, crn, createdByUser!!)
    }
  }

  fun getOutOfRegionReferral(caseEntity: CaseEntity, crn: String): OutOfRegionReferralDto? = outOfRegionReferralRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseEntity.id)
    ?.let { oorEntity ->
      val createdByUser = userRepository.findByIdOrNull(oorEntity.createdByUserId!!)
      OutOfRegionReferralTransformer.toOutOfRegionReferralDto(oorEntity, crn, createdByUser!!.displayName())
    }

  fun getOutOfRegionReferral(crn: String, id: UUID): OutOfRegionReferralDto {
    val entity = outOfRegionReferralRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val createdByUser = userRepository.findByIdOrNull(entity.createdByUserId!!)

    return OutOfRegionReferralTransformer.toOutOfRegionReferralDto(
      entity,
      crn,
      createdByUser!!.displayName(),
      active = entity.id == getActiveOorId(entity.caseId),
    )
  }

  fun getOutOfRegionReferral(id: UUID): OutOfRegionReferralDto {
    val oorEntity = outOfRegionReferralRepository.findByIdOrNull(id).orThrowNotFound("id" to id)
    val caseEntity = caseRepository.findWithIdentifiersById(oorEntity.caseId).orThrowNotFound("id" to id)
    val createdByUser = userRepository.findByIdOrNull(oorEntity.createdByUserId!!)

    return OutOfRegionReferralTransformer.toOutOfRegionReferralDto(
      oorEntity,
      crn = caseEntity.latestCrn(),
      createdByUser!!.displayName(),
    )
  }

  fun getOutOfRegionReferralTimeline(id: UUID, crn: String): ApiResponseDto<List<AuditRecordDto>> {
    val oorEntity = outOfRegionReferralRepository.findByIdAndCrnWithNotes(id, crn).orThrowNotFound("id" to id, "crn" to crn)

    val auditHistory = auditService.fullAuditHistory(oorEntity.id, OutOfRegionReferralEntity::class.java)
    val noteTimelineRecords =
      if (oorEntity.notes.isNotEmpty()) getOutOfRegionReferralNotesTimeline(oorEntity) else emptyList()

    val timelineRecords = (auditHistory + noteTimelineRecords).sortedByDescending { it.commitDate }

    return toApiResponseDto(
      data = timelineRecords,
    )
  }

  private fun getOutOfRegionReferralNotesTimeline(oorEntity: OutOfRegionReferralEntity): List<AuditRecordDto> {
    val createdByUserIds = oorEntity.notes.mapNotNull { it.createdByUserId }.toSet()
    val createdByUsers = userRepository.findAllById(createdByUserIds).associateBy { it.id }
    return oorEntity.notes.map {
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
