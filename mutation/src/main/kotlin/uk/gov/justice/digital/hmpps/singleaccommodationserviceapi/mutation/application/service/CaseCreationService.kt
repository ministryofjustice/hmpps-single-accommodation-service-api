package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import java.time.LocalDate

@Service
class CaseCreationService(
  private val caseOrchestrationService: CaseMutationOrchestrationService,
  private val caseSnapshotAssembler: CaseSnapshotAssembler,
  private val caseRepository: CaseRepository,
  private val caseMapper: CaseMapper,
  private val entityManager: EntityManager,
) {

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun saveUnpersistedCasesAsBlankRows(casesToCreate: List<CaseToCreate>) {
    val unpersistedCrns = caseRepository
      .findUnpersistedCrns(casesToCreate.map { it.crn }.toTypedArray())
      .toSet()

    if (unpersistedCrns.isEmpty()) {
      return
    }

    casesToCreate
      .filter { it.crn in unpersistedCrns }
      .map {
        caseMapper.create(
          snapshot = CaseAggregate.hydrateNew(
            firstName = it.firstName,
            lastName = it.lastName,
            dateOfBirth = it.dateOfBirth,
          ).snapshot(),
          crn = it.crn,
          prisonNumber = it.prisonNumber,
        )
      }
      .forEach(entityManager::persist)
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun saveUnpersistedCases(casesToCreate: List<CaseToCreate>) {
    val unpersistedCrns = caseRepository
      .findUnpersistedCrns(casesToCreate.map { it.crn }.toTypedArray())
      .toSet()

    if (unpersistedCrns.isEmpty()) {
      return
    }
    casesToCreate
      .filter { it.crn in unpersistedCrns }
      .forEach {
        upsertCase(it.crn, it.prisonNumber)
      }
  }

  @Transactional
  fun upsertCase(crn: String, prisonNumber: String?): CaseEntity {
    val caseDto = caseOrchestrationService.getCurrentCaseResult(crn = crn, prisonNumber = prisonNumber).data

    val existingCase = caseRepository.findByIdentifiers(
      crns = listOf(crn),
      prisonNumbers = prisonNumber?.let(::listOf),
    )

    val aggregate = existingCase?.let(caseMapper::toAggregate) ?: CaseAggregate.hydrateNew()
    caseSnapshotAssembler.upsertCase(aggregate, caseDto)

    val entity = existingCase?.let {
      caseMapper.merge(it, aggregate.snapshot())
    } ?: caseMapper.create(snapshot = aggregate.snapshot(), crn = crn, prisonNumber = prisonNumber)

    return caseRepository.save(entity)
  }
}

data class CaseToCreate(
  val crn: String,
  val prisonNumber: String?,
  val firstName: String? = null,
  val lastName: String? = null,
  val dateOfBirth: LocalDate? = null,
)
