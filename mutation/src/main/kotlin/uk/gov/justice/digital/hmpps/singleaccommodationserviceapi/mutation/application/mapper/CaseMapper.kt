package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseIdentifierEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate

@Component
class CaseMapper {

  private fun buildIdentifiers(crn: String, prisonNumber: String?) = buildMap {
    put(crn, IdentifierType.CRN)
    prisonNumber?.let { put(it, IdentifierType.PRISON_NUMBER) }
  }

  fun toAggregate(entity: CaseEntity): CaseAggregate = CaseAggregate.hydrate(
    id = entity.id,
    tierScore = entity.tierScore,
    hasSyncedCprProposedAccommodation = entity.hasSyncedCprProposedAccommodation,
    firstName = entity.firstName,
    lastName = entity.lastName,
    dateOfBirth = entity.dateOfBirth,
    currentAccommodation = entity.currentAccommodation,
    nextAccommodation = entity.nextAccommodation,
    accommodationStatus = entity.accommodationStatus,
    roshLevelCode = entity.roshLevelCode,
  )

  fun create(snapshot: CaseAggregate.CaseSnapshot, crn: String, prisonNumber: String?): CaseEntity {
    val entity = CaseEntity(
      id = snapshot.id,
      tierScore = snapshot.tierScore,
      hasSyncedCprProposedAccommodation = snapshot.hasSyncedCprProposedAccommodation,
      firstName = snapshot.firstName,
      lastName = snapshot.lastName,
      dateOfBirth = snapshot.dateOfBirth,
      currentAccommodation = snapshot.currentAccommodation,
      nextAccommodation = snapshot.nextAccommodation,
      accommodationStatus = snapshot.accommodationStatus,
      roshLevelCode = snapshot.roshLevelCode,
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
    entity.firstName = snapshot.firstName
    entity.lastName = snapshot.lastName
    entity.dateOfBirth = snapshot.dateOfBirth
    entity.currentAccommodation = snapshot.currentAccommodation
    entity.nextAccommodation = snapshot.nextAccommodation
    entity.accommodationStatus = snapshot.accommodationStatus
    entity.roshLevelCode = snapshot.roshLevelCode

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
