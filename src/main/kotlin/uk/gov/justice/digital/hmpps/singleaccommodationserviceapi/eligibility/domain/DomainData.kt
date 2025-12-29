package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.TierScore
import java.time.LocalDate

data class DomainData(
  val crn: String,
  val tier: TierScore,
  val sex: Sex,
  val releaseDate: LocalDate?,
  val cas1Application: Cas1Application? = null,
) {
  constructor(crn: String, cpr: CorePersonRecord, tier: Tier, prisonerData: List<Prisoner>, cas1Application: Cas1Application?) : this(
    crn = crn,
    tier = tier.tierScore,
    sex = cpr.sex ?: error("Sex must not be null for DomainData"),
    releaseDate = prisonerData.mapNotNull { it.releaseDate }
      .maxByOrNull { it },
    cas1Application = cas1Application,
  )
}
