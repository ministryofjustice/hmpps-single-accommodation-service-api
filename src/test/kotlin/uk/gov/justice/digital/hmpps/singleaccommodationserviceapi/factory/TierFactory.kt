package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.Tier
import java.time.LocalDateTime
import java.util.UUID

fun buildTier() = Tier(
  tierScore = "TODO()",
  calculationId = UUID.randomUUID(),
  calculationDate = LocalDateTime.now(),
  changeReason = "TODO()",
)
