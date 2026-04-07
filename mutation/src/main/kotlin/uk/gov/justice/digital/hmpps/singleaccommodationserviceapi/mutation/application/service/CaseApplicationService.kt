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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate

@Service
class CaseApplicationService(
  private val caseRepository: CaseRepository,
  private val caseOrchestrationService: CaseMutationOrchestrationService,
  private val corePersonRecordCachingService: CorePersonRecordCachingService,
) {
  private val log = LoggerFactory.getLogger(CaseApplicationService::class.java)

  @Transactional
  fun upsertCases(crns: List<String>) {
    val missingCrns = caseRepository.findMissingCrns(crns = crns.toTypedArray())
    if (missingCrns.isNotEmpty()) {
      val casesToAdd = caseOrchestrationService.getCases(missingCrns)
      casesToAdd.forEach { case ->
        val identifiers = case.cpr?.identifiers
        if (identifiers != null) {
          val caseEntity = findByAndUpdatePersonIdentifiers(identifiers)
          upsertCase(crn = case.crn, caseEntity, identifiers)
        } else {
          log.error("No CPR identifiers found for case ${case.crn} not inserting a new case to avoid duplicates")
        }
      }
    }
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
