package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.InvalidCrnsException

@Service
class CaseApplicationService(
  private val caseCreationService: CaseCreationService,
  private val caseRepository: CaseRepository,
  private val approvedPremisesAndDeliusCachingService: ApprovedPremisesAndDeliusCachingService,
) {
  private val log = LoggerFactory.getLogger(CaseApplicationService::class.java)
  private val maxAttempts = 3

  fun createCases(
    crnsToPrisonNumbers: List<CrnToPrisonNumber>,
    createAsBlankRecord: Boolean,
    validateCrns: Boolean = false,
  ) {
    if (validateCrns) {
      validateUnpersistedCrns(crnsToPrisonNumbers.map { it.crn })
    }

    crnsToPrisonNumbers.chunked(25).forEach {
      saveChunkWithRetry(chunk = it, createAsBlankRecord)
    }
  }

  private fun validateUnpersistedCrns(crns: List<String>) {
    val unpersistedCrns = caseRepository.findUnpersistedCrns(crns.distinct().toTypedArray())
    if (unpersistedCrns.isEmpty()) return

    val validCrns = approvedPremisesAndDeliusCachingService.postCaseSummaries(unpersistedCrns).cases
      .map { it.crn }
      .toSet()

    val invalidCrns = unpersistedCrns - validCrns
    if (invalidCrns.isNotEmpty()) throw InvalidCrnsException(invalidCrns)
  }

  private fun saveChunkWithRetry(chunk: List<CrnToPrisonNumber>, createAsBlankRecord: Boolean) {
    repeat(maxAttempts) { attempt ->
      try {
        if (createAsBlankRecord) {
          caseCreationService.saveUnpersistedCasesAsBlankRows(chunk)
        } else {
          caseCreationService.saveUnpersistedCases(chunk)
        }
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
}

data class CrnToPrisonNumber(val crn: String, val prisonNumber: String?)
