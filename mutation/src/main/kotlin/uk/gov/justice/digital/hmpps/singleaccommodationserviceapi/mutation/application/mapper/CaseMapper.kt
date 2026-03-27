package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseIdentifierEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate

object CaseMapper {

  fun toAggregate(entity: CaseEntity): CaseAggregate = CaseAggregate.hydrate(
    id = entity.id,
    tierScore = entity.tierScore,
  )

  fun merge(
    entity: CaseEntity,
    snapshot: CaseAggregate.CaseSnapshot,
    identifiers: Map<String, IdentifierType>? = null,
  ): CaseEntity {
    entity.tierScore = snapshot.tier
    identifiers?.let { entity.addMissingIdentifiers(it) }

    return entity
  }

  fun CaseEntity.addMissingIdentifiers(snapshotIdentifiers: Map<String, IdentifierType>) {
    val existingIdentifiers = this.caseIdentifiers.associate { it.identifier to it.identifierType }

    snapshotIdentifiers.forEach { (identifier, type) ->
      if (existingIdentifiers[identifier] != type) {
        caseIdentifiers.add(
          CaseIdentifierEntity(
            identifier = identifier,
            identifierType = type,
            caseEntity = this,
          ),
        )
      }
    }
  }
}
