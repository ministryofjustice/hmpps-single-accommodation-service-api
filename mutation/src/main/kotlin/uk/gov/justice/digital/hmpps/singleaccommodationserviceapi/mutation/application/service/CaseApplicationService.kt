package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class CaseApplicationService(
  private val caseCreationService: CaseCreationService,
) {
  private val log = LoggerFactory.getLogger(CaseApplicationService::class.java)
  private val maxAttempts = 3

  fun createCases(crnsToPrisonNumbers: List<CrnToPrisonNumber>, createAsBlankRecord: Boolean) {
    crnsToPrisonNumbers.chunked(25).forEach {
      saveChunkWithRetry(chunk = it, createAsBlankRecord)
    }
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
