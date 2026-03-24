package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.PersonTransformer.toPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import java.util.UUID

@Service
class CaseApplicationService(
  private val caseRepository: CaseRepository,
  private val caseApplicationOrchestrationService: CaseApplicationOrchestrationService,
  private val userService: UserService,
) {

  @Transactional
  fun upsertCases(): List<PersonDto> {
    val user = userService.authorizeAndRetrieveUser()

    // new pi endpoint
    val caseList = caseApplicationOrchestrationService.getCaseList(user.username)

    val crns = caseList.cases.map { it.crn }

    val caseEntities = caseRepository.findByCrns(crns)

    val missingCrns = crns.filter { crn -> !caseEntities.map { it.crn }.contains(crn) }

    if (missingCrns.isNotEmpty()) {
      // assume a row from the case table is always complete, so only need to get fresh cases if that row is missing
      val freshCases = caseApplicationOrchestrationService.getFreshCases(missingCrns)
      upsertFreshCases(freshCases)
    }

    return caseList.cases.map { toPersonDto(it) }
  }

  fun upsertFreshCases(
    freshCases: List<CaseApplicationOrchestrationDto>,
  ) {
    freshCases.forEach { freshCase ->

      val aggregate = CaseAggregate.createNew(
        id = UUID.randomUUID(),
        crn = freshCase.crn,
      )

      aggregate.upsertCase(
        freshCase = freshCase,
      )

      caseRepository.save(
        CaseMapper.toEntity(aggregate.snapshot()),
      )
    }
  }

  @Transactional
  fun upsertTier(
    tier: Tier,
    crn: String,
  ) {
    val case = caseRepository.findByCrn(crn)
    val aggregate = case?.let {
      CaseMapper.toAggregate(it)
    } ?: CaseAggregate.createNew(
      id = UUID.randomUUID(),
      crn = crn,

    )
    aggregate.upsertTier(
      newTier = tier.tierScore,
    )
    caseRepository.save(
      CaseMapper.toEntity(aggregate.snapshot()),
    )
  }
}
