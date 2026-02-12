package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate.CaseSnapshot
import java.util.UUID

fun buildCaseSnapshot(
  crn: String = "X123456",
  tier: TierScore? = null,
) = CaseSnapshot(
  id = UUID.randomUUID(),
  crn = crn,
  tier = tier,
)
