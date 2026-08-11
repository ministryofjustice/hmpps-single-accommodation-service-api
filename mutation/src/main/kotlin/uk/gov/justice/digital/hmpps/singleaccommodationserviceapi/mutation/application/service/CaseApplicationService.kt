package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate

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
  fun upsertCase(crn: String, prisonNumber: String?): CaseEntity {
    val caseDto = caseOrchestrationService.getCase(crn)

    val existingCase = caseRepository.findByIdentifiers(
      crns = listOf(crn),
      prisonNumbers = prisonNumber?.let(::listOf),
    )

    val snapshot = (existingCase?.let(CaseMapper::toAggregate) ?: CaseAggregate.hydrateNew())
      .upsertCase(caseDto)
      .snapshot()

    val entity = existingCase?.let {
      CaseMapper.merge(it, snapshot)
    } ?: CaseMapper.create(snapshot = snapshot, crn = crn, prisonNumber = prisonNumber)

    return caseRepository.save(entity)
  }

  private fun CaseAggregate.upsertCase(caseMutationOrchestrationDto: CaseMutationOrchestrationDto): CaseAggregate = this.upsertCase(
    tierScore = caseMutationOrchestrationDto.tier?.tierScore,
  )

  @Transactional
  fun updateTier(tier: Tier, crn: String) {
    val caseEntity: CaseEntity = caseRepository.findByCrn(crn) ?: return

    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    caseAggregate.updateTier(tier.tierScore)
    caseRepository.save(CaseMapper.merge(caseEntity, caseAggregate.snapshot()))
  }
}

data class CrnToPrisonNumber(val crn: String, val prisonNumber: String?)
