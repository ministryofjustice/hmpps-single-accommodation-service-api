package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate.CaseSnapshot
import java.util.UUID

fun buildCaseSnapshot(
  tier: TierScore? = null,
) = CaseSnapshot(
  id = UUID.randomUUID(),
  tier = tier,
)
