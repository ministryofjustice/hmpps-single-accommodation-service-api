package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseIdentifierEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate.CaseIdentifier
import java.util.UUID

object CaseMapper {

  fun creatNew() = CaseEntity(
    id = UUID.randomUUID(),
  )

  fun toAggregate(entity: CaseEntity): CaseAggregate = CaseAggregate.hydrate(
    id = entity.id,
    tierScore = entity.tierScore,
    caseIdentifiers = entity.caseIdentifiers.toCaseIdentifiers(),
    cas1ApplicationId = entity.cas1ApplicationId,
    cas1ApplicationApplicationStatus = entity.cas1ApplicationApplicationStatus,
    cas1ApplicationRequestForPlacementStatus = entity.cas1ApplicationRequestForPlacementStatus,
    cas1ApplicationPlacementStatus = entity.cas1ApplicationPlacementStatus,
  )

  fun MutableSet<CaseIdentifierEntity>.toCaseIdentifiers() = this.map {
    CaseIdentifier(
      it.id,
      it.identifier,
      it.identifierType,
    )
  }.toMutableSet()

  fun merge(
    entity: CaseEntity,
    snapshot: CaseAggregate.CaseSnapshot,
    identifiers: Map<String, IdentifierType>? = null,
  ): CaseEntity {
    entity.tierScore = snapshot.tierScore
    entity.cas1ApplicationId = snapshot.cas1ApplicationId
    entity.cas1ApplicationApplicationStatus = snapshot.cas1ApplicationApplicationStatus
    entity.cas1ApplicationRequestForPlacementStatus = snapshot.cas1ApplicationRequestForPlacementStatus
    entity.cas1ApplicationPlacementStatus = snapshot.cas1ApplicationPlacementStatus

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
