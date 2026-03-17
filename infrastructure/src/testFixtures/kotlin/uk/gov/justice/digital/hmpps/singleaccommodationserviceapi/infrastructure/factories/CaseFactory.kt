package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import java.util.UUID

fun buildCaseEntity(
  id: UUID = UUID.randomUUID(),
  tier: TierScore? = null,
  identifier: String = "X12345",
  identifierType: IdentifierType = IdentifierType.CRN,
  additionalCaseIdentifiers: Map<String, IdentifierType> = mapOf(),
) = CaseEntity(id = id, tier = tier).also { case ->
  case.addIdentifier(
    identifier = identifier,
    identifierType = identifierType,
  )
  additionalCaseIdentifiers.forEach { case.addIdentifier(it.key, it.value) }
}
