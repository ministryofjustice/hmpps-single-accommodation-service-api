package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseIdentifierEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import java.time.Instant
import java.util.UUID

fun buildCaseEntity(
  id: UUID = UUID.randomUUID(),
  identifier: String = UUID.randomUUID().toString(),
  identifierType: IdentifierType = IdentifierType.CRN,
  tier: TierScore? = TierScore.A1,
) = CaseEntity(id = id, tierScore = tier).also { case ->
  case.caseIdentifiers =
    mutableSetOf(buildCaseIdentifier(identifier = identifier, identifierType = identifierType, caseEntity = case))
}

fun buildCaseIdentifier(
  id: UUID = UUID.randomUUID(),
  caseEntity: CaseEntity,
  identifier: String = "DEFAULT",
  identifierType: IdentifierType = IdentifierType.CRN,
  createdAt: Instant = Instant.now(),
) = CaseIdentifierEntity(
  id = id,
  caseEntity = caseEntity,
  identifier = identifier,
  identifierType = identifierType,
  createdAt = createdAt,
)
