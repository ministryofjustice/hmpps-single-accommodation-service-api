package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailureTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.Identifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper.addMissingIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate

@Service
class CaseApplicationService(
  private val caseRepository: CaseRepository,
  private val caseOrchestrationService: CaseMutationOrchestrationService,
  private val corePersonRecordCachingService: CorePersonRecordCachingService,
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
  fun upsertCases(caseDtos: List<CaseMutationOrchestrationDto>, crnToPrisonNumber: Map<String, String?>) {
    val caseByIdentifier = mapPersistedIdentifiersToCase(caseDtos).toMutableMap()
    val casesToPersist = mutableSetOf<CaseEntity>()

    caseDtos.forEach { caseDto ->

      val cprIds = caseDto.cpr?.identifiers ?: run {
        log.error("No identifiers returned from CPR for CRN {}", caseDto.crn)
        return@forEach
      }

      val identifiersFromDelius = buildIdentifiers(crn = caseDto.crn, prisonNumber = crnToPrisonNumber[caseDto.crn])

      // map the identifiers from CPR, to check if any are already known to SAS
      val identifiersFromCpr = buildMap {
        cprIds.crns.associateWith { IdentifierType.CRN }.let { putAll(it) }
        cprIds.prisonNumbers.associateWith { IdentifierType.PRISON_NUMBER }.let { putAll(it) }
      }

      // try to find any existing case with the identifiers
      val existingCase = identifiersFromCpr.firstNotNullOfOrNull { (identifier, identifierType) ->
        caseByIdentifier[identifier to identifierType]
      }

      // if there is a match, update the case with the latest CRN and ON from delius, or create a new entity
      val caseEntity = if (existingCase != null) {
        log.info("Updating case: [{}] with identifiers: {}", existingCase.id, identifiersFromDelius)
        existingCase.addMissingIdentifiers(identifiersFromDelius)
        existingCase
      } else {
        log.info("Creating new Case with identifiers: {}", identifiersFromDelius)
        CaseMapper.create(
          CaseAggregate.hydrateNew().snapshot(),
          identifiersFromDelius,
        )
      }

      casesToPersist += caseEntity

      // add the new identifiers and existing case to the cases caseByIdentifier, so we don't duplicate.
      identifiersFromDelius.forEach { caseByIdentifier[it.key to it.value] = caseEntity }
    }
    caseRepository.saveAll(casesToPersist)
  }

  private fun mapPersistedIdentifiersToCase(caseDtos: List<CaseMutationOrchestrationDto>): Map<Pair<String, IdentifierType>, CaseEntity> {
    val allCrns = caseDtos
      .flatMap { it.cpr?.identifiers?.crns ?: emptyList() }
      .takeIf { it.isNotEmpty() }

    val allPrisonNumbers = caseDtos
      .flatMap { it.cpr?.identifiers?.prisonNumbers ?: emptyList() }
      .takeIf { it.isNotEmpty() }

    if (allCrns.isNullOrEmpty() && allPrisonNumbers.isNullOrEmpty()) return emptyMap()

    val entities = caseRepository.findAllByIdentifiers(
      crns = allCrns,
      prisonNumbers = allPrisonNumbers,
    )

    return entities.asSequence()
      .flatMap { entity ->
        entity.caseIdentifiers.asSequence().map { identifier ->
          (identifier.identifier to identifier.identifierType) to entity
        }
      }.toMap()
  }

  @Transactional
  fun upsertCase(crn: String, prisonNumber: String?) {
    val caseEntity = caseRepository.findByCrn(crn)
    if (caseEntity != null) {
      updateCase(crn, caseEntity)
    } else {
      val identifiers = getCorePersonRecord(crn, IdentifierType.CRN).identifiers
      requireNotNull(identifiers) { "No identifiers returned from CPR for CRN $crn." }

      val caseEntity =
        findByAndUpdatePersonIdentifiers(crn = crn, prisonNumber = prisonNumber, identifiers = identifiers)

      caseEntity?.let { updateCase(crn, caseEntity) } ?: createNewCase(crn, identifiers)
    }
  }

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

  private fun createNewCase(crn: String, identifiers: Identifiers) {
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
        identifiers = getCaseIdentifiers(identifiers),
      ),
    )
  }

  private fun getCaseIdentifiers(
    identifiers: Identifiers,
  ): Map<String, IdentifierType> = identifiers.crns.associateWith { IdentifierType.CRN } +
    identifiers.prisonNumbers.associateWith { IdentifierType.PRISON_NUMBER }

  fun getCorePersonRecord(identifier: String, identifierType: IdentifierType): CorePersonRecord = when (identifierType) {
    IdentifierType.CRN -> corePersonRecordCachingService.getCorePersonRecordByCrn(identifier)
    IdentifierType.PRISON_NUMBER -> corePersonRecordCachingService.getCorePersonRecordByNoms(identifier)
  }

  @Transactional
  fun findByAndUpdatePersonIdentifiers(
    crn: String,
    prisonNumber: String?,
    identifiers: Identifiers,
  ): CaseEntity? {
    val crns = identifiers.crns
    val prisonNumbers = identifiers.prisonNumbers
    require(crns.isNotEmpty() || prisonNumbers.isNotEmpty()) {
      "At least one identifier must be provided"
    }

    return caseRepository.findByIdentifiers(crns = crns, prisonNumbers = prisonNumbers)?.also { entity ->
      entity.addMissingIdentifiers(buildIdentifiers(crn, prisonNumber))
    }
  }

  @Transactional
  fun updateTier(tier: Tier, crn: String) {
    val caseEntity: CaseEntity = caseRepository.findByCrn(crn)
      ?: findByAndUpdatePersonIdentifiers(
        crn = crn,
        prisonNumber = null,
        getCorePersonRecord(crn, IdentifierType.CRN).identifiers!!,
      )
      ?: return

    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    caseAggregate.updateTier(tier.tierScore)
    caseRepository.save(CaseMapper.merge(caseEntity, caseAggregate.snapshot()))
  }

  private fun buildIdentifiers(crn: String, prisonNumber: String?) = buildMap {
    put(crn, IdentifierType.CRN)
    prisonNumber?.let { put(it, IdentifierType.PRISON_NUMBER) }
  }
}
