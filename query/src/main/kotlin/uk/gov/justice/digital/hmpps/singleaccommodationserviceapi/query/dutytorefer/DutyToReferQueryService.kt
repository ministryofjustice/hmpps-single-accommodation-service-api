package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import java.util.UUID

@Service
class DutyToReferQueryService(
  private val dutyToReferRepository: DutyToReferRepository,
  private val userRepository: UserRepository,
  private val localAuthorityAreaRepository: LocalAuthorityAreaRepository,
) {
  fun getDutyToRefer(crn: String): DutyToReferDto {
    val entity = dutyToReferRepository.findFirstByCrnOrderByCreatedAtDesc(crn)
      ?: return DutyToReferTransformer.toNotStartedDto(crn)

    val createdByUser = userRepository.findByIdOrNull(entity.createdByUserId!!)
    val localAuthorityArea = localAuthorityAreaRepository.findByIdOrNull(entity.localAuthorityAreaId)

    return DutyToReferTransformer.toDutyToReferDto(entity, createdByUser!!.name, localAuthorityArea!!.name)
  }

  fun getDutyToRefer(id: UUID): DutyToReferDto {
    val entity = dutyToReferRepository.findById(id).orElseThrow {
      NotFoundException("Duty to refer with id $id not found")
    }
    val createdByUser = userRepository.findByIdOrNull(entity.createdByUserId!!)
    val localAuthorityArea = localAuthorityAreaRepository.findByIdOrNull(entity.localAuthorityAreaId)

    return DutyToReferTransformer.toDutyToReferDto(entity, createdByUser!!.name, localAuthorityArea!!.name)
  }
}
