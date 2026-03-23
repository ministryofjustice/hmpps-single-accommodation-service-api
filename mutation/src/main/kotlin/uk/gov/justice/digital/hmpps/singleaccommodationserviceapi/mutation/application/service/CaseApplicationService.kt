package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.CaseList
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import java.util.UUID

@Service
class CaseApplicationService(
  private val caseRepository: CaseRepository,
  // created a new orchestration service in the mutation layer for cases - does this mean orchestration should probably all move to infrastructure?
  private val caseApplicationOrchestrationService: CaseApplicationOrchestrationService,
) {

  @Transactional
  fun upsertCases(username: String): CaseList {
    // new pi endpoint
    val caseList = caseApplicationOrchestrationService.getCaseList(username)

    val crns = caseList.cases.map { it.crn }

    val caseEntities = caseRepository.findByCrns(crns)

    val missingCrns = crns.filter { crn -> !caseEntities.map { it.crn }.contains(crn) }

    val partialCrns = crns.filter { crn -> caseEntities.map { it.crn }.contains(crn) }

    // needed as we haven't started listening to cas events yet
    val partialCases = caseApplicationOrchestrationService.getPartialCases(partialCrns)

    // assuming a row is always fully populated
    val freshCases = caseApplicationOrchestrationService.getFreshCases(missingCrns) + partialCases
    val prisonerNumbers = freshCases.flatMap { case ->
      val casePrisonNumbers: List<String>? = case.cpr?.identifiers?.prisonNumbers
      casePrisonNumbers ?: emptyList()
    }
    // val prisoners = caseApplicationOrchestrationService.getPrisoners(prisonerNumbers)

    upsertFreshCases(caseList, freshCases, emptyList())
    return caseList
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

  fun upsertFreshCases(
    caseList: CaseList,
    freshCases: List<CaseApplicationOrchestrationDto>,
    prisoners: List<Prisoner>,
  ) {
    freshCases.forEach { freshCase ->
      val caseListItem = caseList.cases.first { it.crn == freshCase.crn }
      val prisoner = prisoners.firstOrNull { it.prisonerNumber == caseListItem.nomsNumber }

      val aggregate = CaseAggregate.createNew(
        id = UUID.randomUUID(),
        crn = caseListItem.crn,
      )

      aggregate.upsertCase(
        newCaseListItem = caseListItem,
        freshCase = freshCase,
        prisoner = prisoner,
      )

      caseRepository.save(
        CaseMapper.toEntity(aggregate.snapshot()),
      )
    }
  }
}
