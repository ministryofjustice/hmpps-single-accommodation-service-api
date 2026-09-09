package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.otheraccommodationreferral

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FieldChange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OorStatus.ACCEPTED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OorStatus.NOT_ACCEPTED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.toAssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OtherAccommodationReferralRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isDtrExpired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto
import java.time.Clock
import java.util.UUID

private val HISTORY_STATUSES = listOf(ACCEPTED, NOT_ACCEPTED)

@Service
class OtherAccommodationReferralQueryService(
  private val otherAccommodationReferralRepository: OtherAccommodationReferralRepository,
  private val userRepository: UserRepository,
  private val caseRepository: CaseRepository,
  private val auditService: AuditService,
  private val clock: Clock,
) {
  fun getOtherAccommodationReferralHistory(crn: String): List<OtherAccommodationReferralDto> {
    val caseEntity = caseRepository.findByCrn(crn) ?: return emptyList()
    return getOtherAccommodationReferralHistory(caseEntity, crn)
  }

  private fun isActiveOor(oor: OtherAccommodationReferralEntity): Boolean = !isDtrExpired(oor.submissionDate, clock)

  private fun getActiveOorId(caseId: UUID): UUID? = otherAccommodationReferralRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId)
    ?.takeIf { isActiveOor(it) }
    ?.id

  fun getOtherAccommodationReferralHistory(caseEntity: CaseEntity, crn: String): List<OtherAccommodationReferralDto> {
    val activeOorId = getActiveOorId(caseEntity.id)

    val oorEntities = otherAccommodationReferralRepository
      .findByCaseIdAndStatusInOrderByCreatedAtDesc(caseEntity.id, HISTORY_STATUSES)
      .filter { it.id != activeOorId }
    if (oorEntities.isEmpty()) return emptyList()

    val createdByUserIds = oorEntities.mapNotNull { it.createdByUserId }.toSet()
    val users = userRepository.findAllById(createdByUserIds).associateBy { it.id }

    return oorEntities.map { oorEntity ->
      val createdByUser = users[oorEntity.createdByUserId]
      OtherAccommodationReferralTransformer.toOtherAccommodationReferralDto(oorEntity, crn, createdByUser!!)
    }
  }

  fun getOtherAccommodationReferral(caseEntity: CaseEntity, crn: String): OtherAccommodationReferralDto? = otherAccommodationReferralRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseEntity.id)
    ?.let { oorEntity ->
      val createdByUser = userRepository.findByIdOrNull(oorEntity.createdByUserId!!)
      OtherAccommodationReferralTransformer.toOtherAccommodationReferralDto(oorEntity, crn, createdByUser!!.displayName())
    }

  fun getOtherAccommodationReferral(crn: String, id: UUID): OtherAccommodationReferralDto {
    val entity = otherAccommodationReferralRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val createdByUser = userRepository.findByIdOrNull(entity.createdByUserId!!)

    return OtherAccommodationReferralTransformer.toOtherAccommodationReferralDto(
      entity,
      crn,
      createdByUser!!.displayName(),
      active = entity.id == getActiveOorId(entity.caseId),
    )
  }

  fun getOtherAccommodationReferral(id: UUID): OtherAccommodationReferralDto {
    val oorEntity = otherAccommodationReferralRepository.findByIdOrNull(id).orThrowNotFound("id" to id)
    val caseEntity = caseRepository.findWithIdentifiersById(oorEntity.caseId).orThrowNotFound("id" to id)
    val createdByUser = userRepository.findByIdOrNull(oorEntity.createdByUserId!!)

    return OtherAccommodationReferralTransformer.toOtherAccommodationReferralDto(
      oorEntity,
      crn = caseEntity.latestCrn(),
      createdByUser!!.displayName(),
    )
  }

  fun getOtherAccommodationReferralTimeline(id: UUID, crn: String): ApiResponseDto<List<AuditRecordDto>> {
    val oorEntity = otherAccommodationReferralRepository.findByIdAndCrnWithNotes(id, crn).orThrowNotFound("id" to id, "crn" to crn)

    val auditHistory = auditService.fullAuditHistory(oorEntity.id, OtherAccommodationReferralEntity::class.java)
    val noteTimelineRecords =
      if (oorEntity.notes.isNotEmpty()) getOtherAccommodationReferralNotesTimeline(oorEntity) else emptyList()

    val timelineRecords = (auditHistory + noteTimelineRecords).sortedByDescending { it.commitDate }

    return toApiResponseDto(
      data = timelineRecords,
    )
  }

  private fun getOtherAccommodationReferralNotesTimeline(oorEntity: OtherAccommodationReferralEntity): List<AuditRecordDto> {
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
