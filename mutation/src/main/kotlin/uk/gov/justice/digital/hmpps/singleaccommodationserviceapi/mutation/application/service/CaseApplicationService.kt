package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import java.util.UUID

@Service
class CaseApplicationService(
  private val caseRepository: CaseRepository,
) {

  @Transactional
  fun upsertTier(
    tier: Tier,
    crn: String,
  ) {
    val case = caseRepository.findByCrn(crn)
    val aggregate = case?.let {
      CaseMapper.toAggregate(it)
    } ?: CaseAggregate.createNew(
      id = UUID.randomUUID(),
      crn = crn,

    )
    aggregate.upsertTier(
      newTier = tier.tierScore,
    )
    caseRepository.save(
      CaseMapper.toEntity(aggregate.snapshot()),
    )
  }
}
