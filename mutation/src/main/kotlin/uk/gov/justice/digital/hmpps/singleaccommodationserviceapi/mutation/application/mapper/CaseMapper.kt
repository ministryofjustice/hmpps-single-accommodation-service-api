package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseIdentifierEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate.CaseIdentifier

object CaseMapper {

  fun toAggregate(entity: CaseEntity): CaseAggregate = CaseAggregate.hydrate(
    id = entity.id,
    tierScore = entity.tierScore,
    caseIdentifiers = entity.caseIdentifiers.toCaseIdentifiers(),
  )

  fun MutableSet<CaseIdentifierEntity>.toCaseIdentifiers() = this.map {
    CaseIdentifier(
      it.id,
      it.identifier,
      it.identifierType,
    )
  }.toMutableSet()

  fun merge(entity: CaseEntity, snapshot: CaseAggregate.CaseSnapshot): CaseEntity {
    entity.tierScore = snapshot.tier
    entity.caseIdentifiers.clear()
    entity.addMissingIdentifiers(snapshot.caseIdentifiers)

    return entity
  }

  fun CaseEntity.addMissingIdentifiers(snapshotIdentifiers: Set<CaseIdentifier>) {
    val existingKeys = caseIdentifiers
      .map { it.identifier to it.identifierType }.toSet()

    snapshotIdentifiers
      .asSequence()
      .filterNot { (it.identifier to it.identifierType) in existingKeys }
      .forEach {
        this.caseIdentifiers.add(
          CaseIdentifierEntity(
            id = it.id,
            identifier = it.identifier,
            identifierType = it.identifierType,
            caseEntity = this,
          ),
        )
      }
  }
}
