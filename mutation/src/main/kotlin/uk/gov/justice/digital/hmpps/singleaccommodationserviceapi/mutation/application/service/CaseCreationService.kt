package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import java.time.LocalDate
import java.util.UUID

@Service
class CaseCreationService(
  private val caseOrchestrationService: CaseMutationOrchestrationService,
  private val caseSnapshotAssembler: CaseSnapshotAssembler,
  private val caseRepository: CaseRepository,
  private val caseMapper: CaseMapper,
  private val entityManager: EntityManager,
  private val caseRefreshRequestService: CaseRefreshRequestService,
) {
  private val log = LoggerFactory.getLogger(javaClass)

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
    val result = caseOrchestrationService.getCurrentCaseResult(crn = crn, prisonNumber = prisonNumber)

    val existingCase = caseRepository.findByIdentifiers(
      crns = listOf(crn),
      prisonNumbers = prisonNumber?.let(::listOf),
    )

    if (existingCase != null && result.upstreamFailures.isNotEmpty()) {
      requestRefresh(existingCase.id, crn, result.upstreamFailures)
      return existingCase
    }

    val aggregate = existingCase?.let(caseMapper::toAggregate) ?: CaseAggregate.hydrateNew()
    caseSnapshotAssembler.upsertCase(aggregate, result.data)

    val entity = existingCase?.let {
      caseMapper.merge(it, aggregate.snapshot())
    } ?: caseMapper.create(snapshot = aggregate.snapshot(), crn = crn, prisonNumber = prisonNumber)

    val savedCase = caseRepository.save(entity)
    if (result.upstreamFailures.isNotEmpty()) {
      requestRefresh(savedCase.id, crn, result.upstreamFailures)
    }
    return savedCase
  }

  private fun requestRefresh(caseId: UUID, crn: String, upstreamFailures: List<UpstreamFailure>) {
    log.warn(
      "Unable to fully upsertCase, requesting refresh [caseId={}, crn={}, failedCalls={}]",
      caseId,
      crn,
      upstreamFailures.map { it.callKey },
    )
    caseRefreshRequestService.requestLiveRefresh(caseId)
  }
}

data class CaseToCreate(
  val crn: String,
  val prisonNumber: String?,
  val firstName: String? = null,
  val lastName: String? = null,
  val dateOfBirth: LocalDate? = null,
)
