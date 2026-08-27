package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate

@Service
class CaseApplicationService(
  private val caseRepository: CaseRepository,
  private val caseOrchestrationService: CaseMutationOrchestrationService,
  private val caseCreationService: CaseCreationService,
  private val caseSnapshotAssembler: CaseSnapshotAssembler,
  private val caseMapper: CaseMapper,
) {
  private val log = LoggerFactory.getLogger(CaseApplicationService::class.java)
  private val maxAttempts = 3

  fun createCases(crnsToPrisonNumbers: List<CrnToPrisonNumber>) {
    crnsToPrisonNumbers.chunked(25).forEach(::saveChunkWithRetry)
  }

  private fun saveChunkWithRetry(chunk: List<CrnToPrisonNumber>) {
    repeat(maxAttempts) { attempt ->
      try {
        caseCreationService.saveUnpersistedCases(chunk)
        return
      } catch (e: DataIntegrityViolationException) {
        if (attempt == maxAttempts - 1) throw e

        log.warn(
          "Data integrity violation creating cases (attempt {}/{}). Retrying.",
          attempt + 1,
          maxAttempts,
        )
      }
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

data class CrnToPrisonNumber(val crn: String, val prisonNumber: String?)
