package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2CourtBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2HdcApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2PrisonBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import java.time.LocalDate

data class DomainData(
  val crn: String,
  val tier: TierScore,
  val sex: SexCode,
  val releaseDate: LocalDate?,
  val cas1Application: Cas1Application? = null,
  val cas2CourtBailApplication: Cas2CourtBailApplication? = null,
  val cas2PrisonBailApplication: Cas2PrisonBailApplication? = null,
  val cas2HdcApplication: Cas2HdcApplication? = null,

  ) {
  constructor(
    crn: String,
    cpr: CorePersonRecord,
    tier: Tier,
    prisonerData: List<Prisoner>,
    cas1Application: Cas1Application?,
    cas2CourtBailApplication: Cas2CourtBailApplication?,
    cas2PrisonBailApplication: Cas2PrisonBailApplication?,
    cas2HdcApplication: Cas2HdcApplication?,
    ) : this(
    crn = crn,
    tier = tier.tierScore,
    sex = cpr.sex?.code ?: error("Sex must not be null for DomainData"),
    releaseDate = prisonerData.mapNotNull { it.releaseDate }
      .maxByOrNull { it },
    cas1Application = cas1Application,
    cas2CourtBailApplication = cas2CourtBailApplication,
    cas2PrisonBailApplication = cas2PrisonBailApplication,
    cas2HdcApplication = cas2HdcApplication,
  )
}
