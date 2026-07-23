package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseProjectionMapper

@Service
class CaseApplicationService(
  private val caseRepository: CaseRepository,
  private val caseOrchestrationService: CaseMutationOrchestrationService,
  private val caseCreationService: CaseCreationService,
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
  fun upsertCase(crn: String, prisonNumber: String?) = caseRepository.findByIdentifiers(crns = listOf(crn), prisonNumbers = prisonNumber?.let { listOf(it) })
    ?.let { updateCase(crn, it) } ?: createNewCase(crn, prisonNumber)

  private fun updateCase(crn: String, caseEntity: CaseEntity) {
    val caseOrchestrationDto = caseOrchestrationService.getCase(crn)
    caseRepository.save(CaseProjectionMapper.merge(caseEntity, caseOrchestrationDto))
  }

  private fun createNewCase(crn: String, prisonNumber: String?) {
    val caseOrchestrationDto = caseOrchestrationService.getCase(crn)
    caseRepository.save(
      CaseProjectionMapper.create(
        projection = caseOrchestrationDto,
        identifiers = buildIdentifiers(crn, prisonNumber),
      ),
    )
  }
}

fun buildIdentifiers(crn: String, prisonNumber: String?) = buildMap {
  put(crn, IdentifierType.CRN)
  prisonNumber?.let { put(it, IdentifierType.PRISON_NUMBER) }
}
data class CrnToPrisonNumber(val crn: String, val prisonNumber: String?)
