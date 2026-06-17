package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailureTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate

@Service
class CaseApplicationService(
  private val caseRepository: CaseRepository,
  private val caseOrchestrationService: CaseMutationOrchestrationService,
) {
  private val log = LoggerFactory.getLogger(CaseApplicationService::class.java)

  fun findUnpersistedCrns(crns: List<String>) = caseRepository.findUnpersistedCrns(crns = crns.toTypedArray())

  fun getCasesFromOrchestrator(crns: List<String>): ApiResponseDto<List<CaseMutationOrchestrationDto>> {
    val casesOrchestrationResult = caseOrchestrationService.getCases(crns)
    return ApiResponseDto(
      casesOrchestrationResult.data,
      upstreamFailures = casesOrchestrationResult.upstreamFailures.map { UpstreamFailureTransformer.toUpstreamFailureDto(it) },
    )
  }

  @Transactional
  fun upsertCases(caseDtos: List<CaseMutationOrchestrationDto>, crnsToPrisonNumbers: Map<String, String?>) {
    val casesToPersist = caseDtos.map { caseDto ->
      val identifiersFromDelius = buildIdentifiers(crn = caseDto.crn, prisonNumber = crnsToPrisonNumbers[caseDto.crn])
      CaseMapper.create(CaseAggregate.hydrateNew().snapshot(), identifiersFromDelius)
    }
    caseRepository.saveAll(casesToPersist)
      .also { log.debug("Creating new Cases with identifiers: {}", casesToPersist.map { it.caseIdentifiers }) }
  }

  @Transactional
  fun upsertCase(crn: String, prisonNumber: String?) = caseRepository.findByIdentifiers(crns = listOf(crn), prisonNumbers = prisonNumber?.let { listOf(it) })
    ?.let { updateCase(crn, it) } ?: createNewCase(crn, prisonNumber)

  private fun updateCase(crn: String, caseEntity: CaseEntity) {
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    val caseOrchestrationDto = caseOrchestrationService.getCase(crn)
    caseAggregate.upsertCase(
      tierScore = caseOrchestrationDto.tier?.tierScore,
      cas1ApplicationId = caseOrchestrationDto.cas1Application?.id,
      cas1ApplicationApplicationStatus = caseOrchestrationDto.cas1Application?.applicationStatus,
      cas1ApplicationRequestForPlacementStatus = caseOrchestrationDto.cas1Application?.requestForPlacementStatus,
      cas1ApplicationPlacementStatus = caseOrchestrationDto.cas1Application?.placementStatus,
    )
    caseRepository.save(
      CaseMapper.merge(
        entity = caseEntity,
        snapshot = caseAggregate.snapshot(),
      ),
    )
  }

  private fun createNewCase(crn: String, prisonNumber: String?) {
    val caseAggregate = CaseAggregate.hydrateNew()
    val caseOrchestrationDto = caseOrchestrationService.getCase(crn)
    caseAggregate.upsertCase(
      tierScore = caseOrchestrationDto.tier?.tierScore,
      cas1ApplicationId = caseOrchestrationDto.cas1Application?.id,
      cas1ApplicationApplicationStatus = caseOrchestrationDto.cas1Application?.applicationStatus,
      cas1ApplicationRequestForPlacementStatus = caseOrchestrationDto.cas1Application?.requestForPlacementStatus,
      cas1ApplicationPlacementStatus = caseOrchestrationDto.cas1Application?.placementStatus,
    )
    caseRepository.save(
      CaseMapper.create(
        snapshot = caseAggregate.snapshot(),
        identifiers = buildIdentifiers(crn, prisonNumber),
      ),
    )
  }

  @Transactional
  fun updateTier(tier: Tier, crn: String) {
    val caseEntity: CaseEntity = caseRepository.findByCrn(crn) ?: return

    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    caseAggregate.updateTier(tier.tierScore)
    caseRepository.save(CaseMapper.merge(caseEntity, caseAggregate.snapshot()))
  }

  private fun buildIdentifiers(crn: String, prisonNumber: String?) = buildMap {
    put(crn, IdentifierType.CRN)
    prisonNumber?.let { put(it, IdentifierType.PRISON_NUMBER) }
  }
}
