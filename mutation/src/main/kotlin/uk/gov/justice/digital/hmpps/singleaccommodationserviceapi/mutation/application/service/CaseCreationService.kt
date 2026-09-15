package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate

@Service
class CaseCreationService(
  private val caseOrchestrationService: CaseMutationOrchestrationService,
  private val caseSnapshotAssembler: CaseSnapshotAssembler,
  private val caseRepository: CaseRepository,
  private val caseMapper: CaseMapper,
  private val entityManager: EntityManager,
  @param:Value($$"${case-list.v2-enabled}") val caseListV2Enabled: Boolean,
) {

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun saveUnpersistedCasesAsBlankRows(crnsToPrisonNumbers: List<CrnToPrisonNumber>) {
    val unpersistedCrns = caseRepository
      .findUnpersistedCrns(crnsToPrisonNumbers.map { it.crn }.toTypedArray())
      .toSet()

    if (unpersistedCrns.isEmpty()) {
      return
    }

    crnsToPrisonNumbers
      .filter { it.crn in unpersistedCrns }
      .map {
        caseMapper.create(
          snapshot = CaseAggregate.hydrateNew().snapshot(),
          crn = it.crn,
          prisonNumber = it.prisonNumber,
        )
      }
      .forEach(entityManager::persist)
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun saveUnpersistedCases(crnsToPrisonNumbers: List<CrnToPrisonNumber>) {
    if (caseListV2Enabled) {
      val unpersistedCrns = caseRepository
        .findUnpersistedCrns(crnsToPrisonNumbers.map { it.crn }.toTypedArray())
        .toSet()

      if (unpersistedCrns.isEmpty()) {
        return
      }
      crnsToPrisonNumbers
        .filter { it.crn in unpersistedCrns }
        .forEach {
          upsertCase(it.crn, it.prisonNumber, upsertData = caseListV2Enabled)
        }
    } else {
      saveUnpersistedCasesAsBlankRows(crnsToPrisonNumbers)
    }
  }

  @Transactional
  fun upsertCase(crn: String, prisonNumber: String?) = upsertCase(crn = crn, prisonNumber = prisonNumber, upsertData = true)

  @Transactional
  fun upsertCase(crn: String, prisonNumber: String?, upsertData: Boolean): CaseEntity {
    val caseDto = caseOrchestrationService.getCurrentCaseResult(crn = crn, prisonNumber = prisonNumber).data

    val existingCase = caseRepository.findByIdentifiers(
      crns = listOf(crn),
      prisonNumbers = prisonNumber?.let(::listOf),
    )

    val aggregate = existingCase?.let(caseMapper::toAggregate) ?: CaseAggregate.hydrateNew()
    if (upsertData) {
      caseSnapshotAssembler.upsertCase(aggregate, caseDto)
    }

    val entity = existingCase?.let {
      caseMapper.merge(it, aggregate.snapshot())
    } ?: caseMapper.create(snapshot = aggregate.snapshot(), crn = crn, prisonNumber = prisonNumber)

    return caseRepository.save(entity)
  }
}
