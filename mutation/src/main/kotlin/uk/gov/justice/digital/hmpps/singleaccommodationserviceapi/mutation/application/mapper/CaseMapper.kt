package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseIdentifierEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate

object CaseMapper {

  fun toAggregate(entity: CaseEntity): CaseAggregate = CaseAggregate.hydrate(
    id = entity.id,
    tierScore = entity.tierScore,
    caseIdentifiers = entity.caseIdentifiers.toCaseIdentifiers(),
  )

  fun MutableSet<CaseIdentifierEntity>.toCaseIdentifiers() = this.map {
    CaseAggregate.CaseIdentifier(
      it.id,
      it.identifier,
      it.identifierType,
    )
  }.toMutableSet()

  fun merge(entity: CaseEntity, snapshot: CaseAggregate.CaseSnapshot): CaseEntity {
    entity.tierScore = snapshot.tier
    entity.caseIdentifiers.clear()
    entity.caseIdentifiers.addAll(
      snapshot.caseIdentifiers.map {
        CaseIdentifierEntity(
          id = it.id,
          identifier = it.identifier,
          identifierType = it.identifierType,
          caseEntity = entity,
        )
      },
    )

    return entity
  }
}
