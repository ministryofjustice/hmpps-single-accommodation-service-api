package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

  fun getCasesFromOrchestrator(crns: List<String>) = caseOrchestrationService.getCases(crns)

  @Transactional
  fun upsertCases(caseDtos: List<CaseMutationOrchestrationDto>) {
    val caseByIdentifier = mapPersistedIdentifiersToCase(caseDtos)

    val casesToPersist = mutableSetOf<CaseEntity>()

    // TODO this can change to filter successful results when we switch to v2
    caseDtos.filter { it.cpr != null }.forEach { caseDto ->
      val identifiersFromCPR = caseDto.cpr?.identifiers?.let { ids ->
        ids.crns.map { it to IdentifierType.CRN } + ids.prisonNumbers.map { it to IdentifierType.PRISON_NUMBER }
      }

      if (identifiersFromCPR.isNullOrEmpty()) {
        // TODO adding this to maintain previous functionality. We can update when comment above is addressed.
        log.warn("No identifiers returned from CPR for CRN: {}. Skipping case upsert for this record.", caseDto.crn)
        return@forEach
      }

      val caseToUpdate = identifiersFromCPR.firstNotNullOfOrNull { caseByIdentifier[it.first to it.second] }

      if (caseToUpdate != null) {
        log.debug("Updating identifiers for case: {}", caseToUpdate.id)
        caseToUpdate.addMissingIdentifiers(identifiersFromCPR.toMap())
        casesToPersist += caseToUpdate
      } else {
        log.debug("Creating new case with identifiers: {}", identifiersFromCPR)
        casesToPersist += CaseMapper.create(CaseAggregate.hydrateNew().snapshot(), identifiersFromCPR.toMap())
      }
    }
    caseRepository.saveAll(casesToPersist)
    log.info("Successfully upserted {} cases", casesToPersist.size)
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
  fun upsertCase(crn: String) {
    val caseEntity = findByIdentifier(crn, IdentifierType.CRN)
    if (caseEntity != null) {
      updateCase(crn, caseEntity)
    } else {
      val identifiers = getCorePersonRecord(crn, IdentifierType.CRN).identifiers!!
      val caseEntity = findByAndUpdatePersonIdentifiers(identifiers = identifiers)
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

  fun findByIdentifier(identifier: String, identifierType: IdentifierType) = caseRepository.findByIdentifier(identifier, identifierType)

  @Transactional
  fun findByAndUpdatePersonIdentifiers(
    identifiers: Identifiers,
  ): CaseEntity? {
    val crns = identifiers.crns
    val prisonNumbers = identifiers.prisonNumbers
    require(crns.isNotEmpty() || prisonNumbers.isNotEmpty()) {
      "At least one identifier must be provided"
    }
    return caseRepository.findByIdentifiers(crns = crns, prisonNumbers = prisonNumbers)?.also { caseEntity ->
      CaseMapper.toAggregate(caseEntity).also { caseAggregate ->
        val caseIdentifiers = getCaseIdentifiers(identifiers)
        caseRepository.save(CaseMapper.merge(caseEntity, caseAggregate.snapshot(), caseIdentifiers))
      }
    }
  }

  @Transactional
  fun updateTier(tier: Tier, crn: String) {
    val caseEntity: CaseEntity = findByIdentifier(crn, IdentifierType.CRN)
      ?: findByAndUpdatePersonIdentifiers(getCorePersonRecord(crn, IdentifierType.CRN).identifiers!!)
      ?: return

    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    caseAggregate.updateTier(tier.tierScore)
    caseRepository.save(CaseMapper.merge(caseEntity, caseAggregate.snapshot()))
  }
}
