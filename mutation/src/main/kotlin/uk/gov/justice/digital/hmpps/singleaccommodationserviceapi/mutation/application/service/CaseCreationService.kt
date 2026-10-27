package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
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
  private val caseRefreshRequestService: CaseRefreshRequestService,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun saveUnpersistedCasesAsBlankRows(casesToCreate: List<CaseToCreate>) {
    findCasesToCreate(casesToCreate).forEach {
      entityManager.persist(
        caseMapper.create(
          snapshot = CaseAggregate.hydrateNew(
            firstName = it.firstName,
            lastName = it.lastName,
            dateOfBirth = it.dateOfBirth,
          ).snapshot(),
          crn = it.crn,
          prisonNumber = it.prisonNumber,
        ),
      )
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun saveUnpersistedCases(casesToCheck: List<CaseToCreate>) {
    findCasesToCreate(casesToCheck).forEach {
      // this is sequential orchestration. Should really be concurrent.
      val result = caseOrchestrationService.getCurrentCaseResult(crn = it.crn, prisonNumber = it.prisonNumber)
      val aggregate = CaseAggregate.hydrateNew()
      caseSnapshotAssembler.upsertCase(aggregate, result.data)
      val entity = caseMapper.create(snapshot = aggregate.snapshot(), crn = it.crn, prisonNumber = it.prisonNumber)
      caseRepository.save(entity)
      if (result.upstreamFailures.isNotEmpty()) {
        log.warn("Upstream failures for CRN: [{}] {}", it.crn, result.upstreamFailures)
        caseRefreshRequestService.requestLiveRefresh(entity.id)
      }
    }
  }

  private fun findCasesToCreate(casesToCreate: List<CaseToCreate>): List<CaseToCreate> {
    val unpersistedCrns = caseRepository
      .findUnpersistedCrns(casesToCreate.map { it.crn }.toTypedArray())
      .toSet()

    return casesToCreate
      .filter { it.crn in unpersistedCrns }
      .distinctBy { it.crn }
  }
}

data class CaseToCreate(
  val crn: String,
  val prisonNumber: String?,
  val firstName: String? = null,
  val lastName: String? = null,
  val dateOfBirth: LocalDate? = null,
)
