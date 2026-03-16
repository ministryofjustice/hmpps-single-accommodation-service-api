package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import java.time.LocalDate
import java.util.UUID

fun buildCaseEntity(
  id: UUID = UUID.randomUUID(),
  crn: String = "X12345",
  tier: TierScore? = null,
  releaseDate: LocalDate? = null,
) = CaseEntity(
  id = id,
  crn = crn,
  tier = tier,
  releaseDate = releaseDate,
)
