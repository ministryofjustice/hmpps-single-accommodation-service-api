package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.Tier
import java.time.OffsetDateTime

data class DomainData(
  val tier: String,
  val sex: Sex,
  val releaseDate: OffsetDateTime?,
) {
  constructor(cpr: CorePersonRecord, tier: Tier, prisoner: Prisoner) : this(
    tier = tier.tierScore,
    sex = cpr.sex ?: error("Sex must not be null for DomainData"),
    releaseDate = prisoner.releaseDate?.atStartOfDay()?.atOffset(java.time.ZoneOffset.UTC),
  )
}
