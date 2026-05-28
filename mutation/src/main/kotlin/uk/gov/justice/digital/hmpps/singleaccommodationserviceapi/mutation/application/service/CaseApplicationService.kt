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

    caseDtos.filter { it.cpr != null }.forEach { caseDto ->

      val latestIdentifiers = buildMap {
        put(caseDto.crn, IdentifierType.CRN)
        crnToPrisonNumber[caseDto.crn]?.let { put(it, IdentifierType.PRISON_NUMBER) }
      }

      val identifiers = buildMap {
        caseDto.cpr?.identifiers?.crns?.associateWith { IdentifierType.CRN }?.let { putAll(it) }
        caseDto.cpr?.identifiers?.prisonNumbers?.associateWith { IdentifierType.PRISON_NUMBER }?.let { putAll(it) }
      }

      val existingCase = identifiers.firstNotNullOfOrNull { (value, type) ->
        caseByIdentifier[value to type]
      }

      val caseEntity = if (existingCase != null) {
        log.debug("Updating identifiers for case: {}", existingCase.id)
        existingCase.addMissingIdentifiers(latestIdentifiers)
        existingCase
      } else {
        log.debug("Creating new Case for CRN: {}", caseDto.crn)
        CaseMapper.create(
          CaseAggregate.hydrateNew().snapshot(),
          latestIdentifiers,
        )
      }

      casesToPersist += caseEntity

      latestIdentifiers.forEach {
        caseByIdentifier[it.key to it.value] = caseEntity
      }
    }
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
      val identifiers = getCorePersonRecord(crn, IdentifierType.CRN).identifiers!!
      val caseEntity =
        findByAndUpdatePersonIdentifiers(crn = crn, prisonNumber = prisonNumber, identifiers = identifiers)
      upsertCase(crn, caseEntity, identifiers)
    }
  }

  private fun upsertCase(crn: String, caseEntity: CaseEntity?, identifiers: Identifiers?) {
    require(caseEntity != null || identifiers != null) {
      "Either caseEntity or identifiers must be provided"
    }
    if (caseEntity != null) {
      updateCase(crn, caseEntity)
    } else {
      createNewCase(crn, identifiers!!)
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
      entity.addMissingIdentifiers(
        buildMap {
          put(crn, IdentifierType.CRN)
          prisonNumber?.let { put(it, IdentifierType.PRISON_NUMBER) }
        },
      )
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
}
