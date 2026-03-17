package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate.CaseSnapshot

object CaseMapper {

  fun toEntity(snapshot: CaseSnapshot): CaseEntity = CaseEntity(
    id = snapshot.id,
    tier = snapshot.tier?.let { TierScore.valueOf(snapshot.tier.name) },
  ).also {
    snapshot.caseIdentifiers.forEach { identifier ->
      it.addIdentifier(
        identifier.identifier,
        identifier.identifierType,
      )
    }
  }

  fun toAggregate(entity: CaseEntity): CaseAggregate = CaseAggregate.hydrate(
    id = entity.id,
    tier = entity.tier,
    caseIdentifiers = entity.caseIdentifiers.map { CaseAggregate.CaseIdentifier(it.identifier, it.identifierType) },
  )
}
