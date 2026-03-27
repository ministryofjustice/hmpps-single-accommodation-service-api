package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

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

@Service
class CaseApplicationService(
  private val caseRepository: CaseRepository,
  private val corePersonRecordCachingService: CorePersonRecordCachingService,
  private val caseOrchestrationService: CaseOrchestrationService,
) {

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
        val caseIdentifiers = crns.associate { it to IdentifierType.CRN } +
          prisonNumbers.associate { it to IdentifierType.PRISON_NUMBER }
        caseRepository.save(CaseMapper.merge(caseEntity, caseAggregate.snapshot(), caseIdentifiers))
      }
    }
  }

  @Transactional
  fun updateTier(tier: Tier, crn: String) {
    val caseEntity = getCase(crn) ?: return
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    caseAggregate.updateTier(tier.tierScore)
    caseRepository.save(CaseMapper.merge(caseEntity, caseAggregate.snapshot()))
  }

  @Transactional
  fun createCase(crn: String) {
    val caseEntity = getCase(crn) ?: return
    val caseAggregate = CaseMapper.toAggregate(caseEntity)
    val caseOrchestrationDto = caseOrchestrationService.getCase(crn)
    caseAggregate.upsertCase(
      tier = caseOrchestrationDto.tier.tierScore,
      cas1ApplicationId = caseOrchestrationDto.cas1Application?.id,
      cas1ApplicationApplicationStatus = caseOrchestrationDto.cas1Application?.applicationStatus,
      cas1ApplicationRequestForPlacementStatus = caseOrchestrationDto.cas1Application?.requestForPlacementStatus,
      cas1ApplicationPlacementStatus = caseOrchestrationDto.cas1Application?.placementStatus,
    )
  }

  @Transactional
  fun getCase(crn: String): CaseEntity? = findByIdentifier(crn, IdentifierType.CRN)
    ?: findByAndUpdatePersonIdentifiers(getCorePersonRecord(crn, IdentifierType.CRN).identifiers!!)
}
