package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummary
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

  fun createCases(crnsToPrisonNumbers: List<CrnToPrisonNumber>, createAsBlankRecord: Boolean) {
    val casesToCreate = crnsToPrisonNumbers.map { CaseToCreate(crn = it.crn, prisonNumber = it.prisonNumber) }
    saveCases(casesToCreate, createAsBlankRecord)
  }

  fun createValidatedCases(crns: List<String>) {
    val distinctCrns = crns.distinct()
    val caseSummariesByCrn = validateUnpersistedCrns(distinctCrns)

    val casesToCreate = distinctCrns.map { crn ->
      val caseSummary = caseSummariesByCrn[crn]
      CaseToCreate(
        crn = crn,
        prisonNumber = caseSummary?.nomsId,
        firstName = caseSummary?.name?.forename,
        lastName = caseSummary?.name?.surname,
        dateOfBirth = caseSummary?.dateOfBirth,
      )
    }

    saveCases(casesToCreate, createAsBlankRecord = true)
  }

  private fun saveCases(casesToCreate: List<CaseToCreate>, createAsBlankRecord: Boolean) {
    casesToCreate.chunked(25).forEach {
      saveChunkWithRetry(chunk = it, createAsBlankRecord)
    }
  }

  private fun validateUnpersistedCrns(crns: List<String>): Map<String, CaseSummary> {
    val unpersistedCrns = caseRepository.findUnpersistedCrns(crns.distinct().toTypedArray())
    if (unpersistedCrns.isEmpty()) return emptyMap()

    val caseSummariesByCrn = approvedPremisesAndDeliusCachingService.postCaseSummaries(unpersistedCrns).cases
      .associateBy { it.crn }

    val invalidCrns = unpersistedCrns - caseSummariesByCrn.keys
    if (invalidCrns.isNotEmpty()) throw InvalidCrnsException(invalidCrns)

    return caseSummariesByCrn
  }

  private fun saveChunkWithRetry(chunk: List<CaseToCreate>, createAsBlankRecord: Boolean) {
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
