package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import java.util.UUID

@Service
class DutyToReferQueryService(
  private val dutyToReferRepository: DutyToReferRepository,
  private val userRepository: UserRepository,
  private val localAuthorityAreaRepository: LocalAuthorityAreaRepository,
  private val caseRepository: CaseRepository,
) {
  fun getDutyToRefer(crn: String): DutyToReferDto {
    val caseEntity = caseRepository.findByCrn(crn).orThrowNotFound("crn" to crn)
    val dtrEntity = dutyToReferRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseEntity.id)
      ?: return DutyToReferTransformer.toNotStartedDto(caseEntity.id, crn)

    val createdByUser = userRepository.findByIdOrNull(dtrEntity.createdByUserId!!)
    val localAuthorityArea = localAuthorityAreaRepository.findByIdOrNull(dtrEntity.localAuthorityAreaId)

    return DutyToReferTransformer.toDutyToReferDto(dtrEntity, crn, createdByUser!!.name, localAuthorityArea!!.name)
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
}
