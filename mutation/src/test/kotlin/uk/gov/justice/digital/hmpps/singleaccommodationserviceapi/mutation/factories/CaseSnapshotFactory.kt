package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate.CaseSnapshot
import java.util.UUID

fun buildCaseSnapshot(
  identifier: String = "X123456",
  identifierType: IdentifierType = IdentifierType.CRN,
  tier: TierScore? = null,
) = CaseSnapshot(
  id = UUID.randomUUID(),
  caseIdentifiers = setOf(CaseAggregate.CaseIdentifier(UUID.randomUUID(), identifier, identifierType)),
  tier = tier,
)
