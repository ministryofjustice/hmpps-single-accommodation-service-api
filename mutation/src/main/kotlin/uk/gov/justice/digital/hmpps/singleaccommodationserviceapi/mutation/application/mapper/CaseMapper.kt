package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseIdentifierEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate

@Component
class CaseMapper(
  private val jsonMapper: JsonMapper,
) {

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
    currentAccommodation = entity.currentAccommodation?.let { jsonMapper.readValue(it, AccommodationSummaryDto::class.java) },
    nextAccommodation = entity.nextAccommodation?.let { jsonMapper.readValue(it, AccommodationSummaryDto::class.java) },
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
      currentAccommodation = snapshot.currentAccommodation?.let { jsonMapper.writeValueAsString(it) },
      nextAccommodation = snapshot.nextAccommodation?.let { jsonMapper.writeValueAsString(it) },
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
    entity.currentAccommodation = snapshot.currentAccommodation?.let { jsonMapper.writeValueAsString(it) }
    entity.nextAccommodation = snapshot.nextAccommodation?.let { jsonMapper.writeValueAsString(it) }
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
