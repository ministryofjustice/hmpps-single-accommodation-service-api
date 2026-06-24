package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import java.time.LocalDateTime
import java.util.UUID

fun buildTier(tierScore: String = "A1") = Tier(
  tierScore = tierScore,
  calculationId = UUID.randomUUID(),
  calculationDate = LocalDateTime.now(),
  changeReason = "TODO()",
)
