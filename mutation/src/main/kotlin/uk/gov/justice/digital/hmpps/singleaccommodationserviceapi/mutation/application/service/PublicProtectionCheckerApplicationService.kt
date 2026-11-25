package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PpcCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.PublicProtectionCheckerEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.PublicProtectionCheckerRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.PublicProtectionCheckerMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.PublicProtectionCheckerAggregate

@Service
class PublicProtectionCheckerApplicationService(
  private val publicProtectionCheckerRepository: PublicProtectionCheckerRepository,
  private val userService: UserService,
  private val caseRepository: CaseRepository,
) {
  @Transactional
  fun createPublicProtectionCheck(crn: String, command: PpcCommand): PublicProtectionCheckerEntity {
    userService.authorizeAndRetrieveUser()
    val case = caseRepository.findByCrn(crn).orThrowNotFound("crn" to crn)
    val aggregate = PublicProtectionCheckerAggregate.create(
      caseId = case.id,
      supervisionRequired = command.supervisionRequired,
      controlMeasuresSuitable = command.controlMeasuresSuitable,
      practicalSupportSufficient = command.practicalSupportSufficient,
    )
    return publicProtectionCheckerRepository.save(PublicProtectionCheckerMapper.toEntity(aggregate.snapshot()))
  }
}
