package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import java.util.UUID

@Service
class CaseApplicationService(
  private val caseRepository: CaseRepository,
  private val corePersonRecordCachingService: CorePersonRecordCachingService,
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

  fun getCaseAggregate(identifier: String, identifierType: IdentifierType): CaseAggregate {
    caseRepository.findByIdentifier(identifier, identifierType)?.let {
      return CaseMapper.toAggregate(it)
    }

    val person = when (identifierType) {
      IdentifierType.CRN -> corePersonRecordCachingService.getCorePersonRecordByCrn(identifier)
      IdentifierType.PRISON_NUMBER -> corePersonRecordCachingService.getCorePersonRecordByNoms(identifier)
    }

    val prisonNumbers = person.identifiers?.prisonNumbers
    val crns = person.identifiers?.crns

    // TODO: This should never happen if we call using a CRN or prison number, but feels like some validation should be done
    require(!crns.isNullOrEmpty() || !prisonNumbers.isNullOrEmpty()) {
      "At least one identifier must be provided"
    }

    val case = caseRepository.findByIdentifiers(prisonNumbers = prisonNumbers, crns = crns)

    if (case != null) {
      person.identifiers?.prisonNumbers?.forEach { case.addIdentifier(it, IdentifierType.PRISON_NUMBER) }
      person.identifiers?.crns?.forEach { case.addIdentifier(it, IdentifierType.CRN) }
    }

    return CaseAggregate.createNew(identifier = identifier, identifierType = identifierType).also {
      caseRepository.save(CaseMapper.toEntity(it.snapshot()))
    }
  }
}
