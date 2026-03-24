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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper.creatNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper.merge
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate.CaseIdentifier
import java.util.UUID

@Service
class CaseApplicationService(
  private val caseRepository: CaseRepository,
  private val caseOrchestrationService: CaseMutationOrchestrationService,
  private val corePersonRecordCachingService: CorePersonRecordCachingService,
) {

  @Transactional
  fun upsertCases(crns: List<String>) {
    val missingCrns = caseRepository.findMissingCrns(crns = crns.toTypedArray())

    if (missingCrns.isNotEmpty()) {
      // assume a row from the case table is always complete, so only need to get fresh cases if that row is missing
      val freshCases = caseOrchestrationService.getCases(missingCrns)
      upsertFreshCases(freshCases)
    }
  }

  private fun upsertFreshCases(
    freshCases: List<CaseMutationOrchestrationDto>,
  ) {
    freshCases.forEach { freshCase ->

      val aggregate = CaseAggregate.createNew(
        id = UUID.randomUUID(),
        caseIdentifiers = mutableSetOf(
          CaseIdentifier(
            UUID.randomUUID(),
            freshCase.crn,
            IdentifierType.CRN,
          ),
        ),
      )

      aggregate.upsertCase(
        tierScore = freshCase.tier?.tierScore,
        cas1ApplicationId = freshCase.cas1Application?.id,
        cas1ApplicationApplicationStatus = freshCase.cas1Application?.applicationStatus,
        cas1ApplicationRequestForPlacementStatus = freshCase.cas1Application?.requestForPlacementStatus,
        cas1ApplicationPlacementStatus = freshCase.cas1Application?.placementStatus,
      )

      val caseIdentifiers = freshCases.associate { it.crn to IdentifierType.CRN }
      caseRepository.save(
        merge(creatNew(), aggregate.snapshot(), caseIdentifiers),
      )
    }
  }

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
        caseRepository.save(merge(caseEntity, caseAggregate.snapshot(), caseIdentifiers))
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
    caseRepository.save(merge(caseEntity, caseAggregate.snapshot()))
  }
}
