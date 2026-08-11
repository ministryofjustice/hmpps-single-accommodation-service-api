package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseIdentifierEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate

object CaseMapper {

  private fun buildIdentifiers(crn: String, prisonNumber: String?) = buildMap {
    put(crn, IdentifierType.CRN)
    prisonNumber?.let { put(it, IdentifierType.PRISON_NUMBER) }
  }

  fun toAggregate(entity: CaseEntity): CaseAggregate = CaseAggregate.hydrate(
    id = entity.id,
    tierScore = entity.tierScore,
    hasSyncedCprProposedAccommodation = entity.hasSyncedCprProposedAccommodation,
  )

  fun create(snapshot: CaseAggregate.CaseSnapshot, crn: String, prisonNumber: String?): CaseEntity {
    val entity = CaseEntity(
      id = snapshot.id,
      tierScore = snapshot.tierScore,
      hasSyncedCprProposedAccommodation = snapshot.hasSyncedCprProposedAccommodation,
    )
    entity.addIdentifiers(buildIdentifiers(crn = crn, prisonNumber = prisonNumber))
    return entity
  }

  fun merge(
    entity: CaseEntity,
    snapshot: CaseAggregate.CaseSnapshot,
    identifiers: Map<String, IdentifierType>? = null,
  ): CaseEntity {
    entity.tierScore = snapshot.tierScore
    entity.hasSyncedCprProposedAccommodation = snapshot.hasSyncedCprProposedAccommodation

    identifiers?.let { entity.addIdentifiers(it) }

    return entity
  }

  fun CaseEntity.addIdentifiers(identifiers: Map<String, IdentifierType>) {
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
