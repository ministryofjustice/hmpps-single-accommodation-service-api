package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate.CaseSnapshot

object CaseMapper {

  fun toEntity(snapshot: CaseSnapshot) = CaseEntity(
    id = snapshot.id,
    crn = snapshot.crn,
    tier = snapshot.tier?.let { TierScore.valueOf(snapshot.tier.name) },
  )

  fun toAggregate(entity: CaseEntity): CaseAggregate = CaseAggregate.hydrate(
    id = entity.id,
    crn = entity.crn,
    tier = entity.tier,
  )
}
