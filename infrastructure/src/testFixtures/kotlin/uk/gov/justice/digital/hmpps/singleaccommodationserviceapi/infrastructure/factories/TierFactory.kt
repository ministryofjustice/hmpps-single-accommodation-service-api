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

fun buildTierV3(tierScore: String = "A", provisional: Boolean = false) = Tier(
  tierScore = tierScore,
  calculationId = UUID.randomUUID(),
  calculationDate = LocalDateTime.now(),
  changeReason = "v3 change reason",
  provisional = provisional,
)
