package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseIdentifierEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate

object CaseMapper {

  fun toAggregate(entity: CaseEntity): CaseAggregate = CaseAggregate.hydrate(
    id = entity.id,
    tierScore = entity.tierScore,
    cas1ApplicationId = entity.cas1ApplicationId,
    cas1ApplicationApplicationStatus = entity.cas1ApplicationApplicationStatus,
    cas1ApplicationRequestForPlacementStatus = entity.cas1ApplicationRequestForPlacementStatus,
    cas1ApplicationPlacementStatus = entity.cas1ApplicationPlacementStatus,
  )

  fun toEntity(snapshot: CaseAggregate.CaseSnapshot, identifiers: Map<String, IdentifierType>): CaseEntity {
    val entity = CaseEntity(
      id = snapshot.id,
      tierScore = snapshot.tier,
      cas1ApplicationId = snapshot.cas1ApplicationId,
      cas1ApplicationApplicationStatus = snapshot.cas1ApplicationApplicationStatus,
      cas1ApplicationRequestForPlacementStatus = snapshot.cas1ApplicationRequestForPlacementStatus,
      cas1ApplicationPlacementStatus = snapshot.cas1ApplicationPlacementStatus,
    )
    identifiers.let { entity.addMissingIdentifiers(identifiers) }
    return entity
  }

  fun merge(
    entity: CaseEntity,
    snapshot: CaseAggregate.CaseSnapshot,
    identifiers: Map<String, IdentifierType>? = null,
  ): CaseEntity {
    entity.tierScore = snapshot.tier
    entity.cas1ApplicationId = snapshot.cas1ApplicationId
    entity.cas1ApplicationApplicationStatus = snapshot.cas1ApplicationApplicationStatus
    entity.cas1ApplicationRequestForPlacementStatus = snapshot.cas1ApplicationRequestForPlacementStatus
    entity.cas1ApplicationPlacementStatus = snapshot.cas1ApplicationPlacementStatus
    identifiers?.let { entity.addMissingIdentifiers(it) }

    return entity
  }

  fun CaseEntity.addMissingIdentifiers(identifiers: Map<String, IdentifierType>) {
    val existingIdentifiers = this.caseIdentifiers.associate { it.identifier to it.identifierType }

    identifiers.forEach { (identifier, type) ->
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
