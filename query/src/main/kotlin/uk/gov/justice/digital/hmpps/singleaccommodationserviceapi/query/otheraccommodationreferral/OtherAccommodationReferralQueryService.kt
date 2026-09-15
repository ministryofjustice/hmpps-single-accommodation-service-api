package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.otheraccommodationreferral

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FieldChange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.toAssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OtherAccommodationReferralRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto
import java.util.UUID

private const val LOCAL_AUTHORITY_AREA_NAME = "localAuthorityAreaName"

@Service
@Transactional(readOnly = true)
class OtherAccommodationReferralQueryService(
  private val otherAccommodationReferralRepository: OtherAccommodationReferralRepository,
  private val userRepository: UserRepository,
  private val localAuthorityAreaRepository: LocalAuthorityAreaRepository,
  private val auditService: AuditService,
) {
  fun getOtherAccommodationReferral(crn: String, id: UUID): OtherAccommodationReferralDto {
    val entity = otherAccommodationReferralRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)

    return OtherAccommodationReferralTransformer.toOtherAccommodationReferralDto(
      entity = entity,
      crn = crn,
    )
  }

  fun getOtherAccommodationReferralTimeline(id: UUID, crn: String): ApiResponseDto<List<AuditRecordDto>> {
    val otherAccommodationReferralEntity = otherAccommodationReferralRepository.findByIdAndCrnWithNotes(id, crn)
      .orThrowNotFound("id" to id, "crn" to crn)

    val auditHistory = auditService.fullAuditHistory(otherAccommodationReferralEntity.id, OtherAccommodationReferralEntity::class.java)
    val otherAccommodationReferralAuditTimelineRecords = getOtherAccommodationReferralTimelineWithLocalAuthorityNames(auditHistory, otherAccommodationReferralEntity.localAuthorityAreaId)

    val noteTimelineRecords =
      if (otherAccommodationReferralEntity.notes.isNotEmpty()) getOtherAccommodationReferralNotesTimeline(otherAccommodationReferralEntity) else emptyList()

    val timelineRecords = (otherAccommodationReferralAuditTimelineRecords + noteTimelineRecords).sortedByDescending { it.commitDate }

    return toApiResponseDto(
      data = timelineRecords,
    )
  }

  private fun getOtherAccommodationReferralTimelineWithLocalAuthorityNames(
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
      createOtherAccommodationReferralTimelineRecordWithLAName(record, laId?.let(laNameById::get))
    }
  }

  private fun createOtherAccommodationReferralTimelineRecordWithLAName(record: AuditRecordDto, localAuthorityAreaName: String?): AuditRecordDto {
    if (localAuthorityAreaName == null) return record
    return record.copy(extraInformation = record.extraInformation.orEmpty() + (LOCAL_AUTHORITY_AREA_NAME to localAuthorityAreaName))
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
