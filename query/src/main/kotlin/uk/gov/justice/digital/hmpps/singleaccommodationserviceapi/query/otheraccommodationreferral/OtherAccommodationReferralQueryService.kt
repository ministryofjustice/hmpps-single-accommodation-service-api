package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.otheraccommodationreferral

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FieldChange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.toAssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OtherAccommodationReferralRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralStatus as EntityOtherAccommodationReferralStatus

@Service
class OtherAccommodationReferralQueryService(
  private val otherAccommodationReferralRepository: OtherAccommodationReferralRepository,
  private val userRepository: UserRepository,
  private val auditService: AuditService,
) {

  fun getOtherAccommodationReferral(crn: String, id: UUID): OtherAccommodationReferralDto {
    val entity = otherAccommodationReferralRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val createdByUser = entity.createdByUserId?.let { userRepository.findByIdOrNull(it) }

    return OtherAccommodationReferralTransformer.toOtherAccommodationReferralDto(
      entity = entity,
      crn = crn,
      createdByUser = createdByUser!!,
    )
  }

  fun searchOtherAccommodationReferrals(
    crn: String,
    statuses: List<OtherAccommodationReferralStatus>?,
  ): List<OtherAccommodationReferralDto> {
    val entities = otherAccommodationReferralRepository.searchByCrn(
      crn = crn,
      statuses = statuses?.takeIf { it.isNotEmpty() }?.map { EntityOtherAccommodationReferralStatus.valueOf(it.name) },
    )
    if (entities.isEmpty()) return emptyList()

    val createdByUsers = userRepository.findAllById(entities.mapNotNull { it.createdByUserId }.toSet()).associateBy { it.id }

    return entities.map { entity ->
      OtherAccommodationReferralTransformer.toOtherAccommodationReferralDto(
        entity = entity,
        crn = crn,
        createdByUser = createdByUsers[entity.createdByUserId]!!,
      )
    }
  }

  fun getOtherAccommodationReferralTimeline(id: UUID, crn: String): ApiResponseDto<List<AuditRecordDto>> {
    val otherAccommodationReferralEntity = otherAccommodationReferralRepository.findByIdAndCrnWithNotes(id, crn)
      .orThrowNotFound("id" to id, "crn" to crn)

    val auditHistory = auditService.fullAuditHistory(otherAccommodationReferralEntity.id, OtherAccommodationReferralEntity::class.java)

    val noteTimelineRecords =
      if (otherAccommodationReferralEntity.notes.isNotEmpty()) getOtherAccommodationReferralNotesTimeline(otherAccommodationReferralEntity) else emptyList()

    val timelineRecords = (auditHistory + noteTimelineRecords).sortedByDescending { it.commitDate }

    return toApiResponseDto(
      data = timelineRecords,
    )
  }

  private fun getOtherAccommodationReferralNotesTimeline(otherAccommodationReferralEntity: OtherAccommodationReferralEntity): List<AuditRecordDto> {
    val createdByUserIds = otherAccommodationReferralEntity.notes.mapNotNull { it.createdByUserId }.toSet()
    val createdByUsers = userRepository.findAllById(createdByUserIds).associateBy { it.id }
    return otherAccommodationReferralEntity.notes.map {
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
