package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain.Accommodation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain.Crs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain.DutyToRefer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2CourtBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2HdcApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2PrisonBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
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
  val currentAccommodation: Accommodation? = null,
  val nextAccommodation: Accommodation? = null,
  val cas1Application: Cas1Application? = null,
  val cas2CourtBailApplication: Cas2CourtBailApplication? = null,
  val cas2PrisonBailApplication: Cas2PrisonBailApplication? = null,
  val cas2HdcApplication: Cas2HdcApplication? = null,
  val cas3Application: Cas3Application? = null,
  val dtrStatus: String? = null,
  val crsStatus: String? = null,
) {
  constructor(
    crn: String,
    cpr: CorePersonRecord,
    tier: Tier,
    prisonerData: List<Prisoner>,
    currentAccommodation: Accommodation? = null,
    proposedAddresses: List<Accommodation>? = null,
    cas1Application: Cas1Application?,
    cas2CourtBailApplication: Cas2CourtBailApplication?,
    cas2PrisonBailApplication: Cas2PrisonBailApplication?,
    cas2HdcApplication: Cas2HdcApplication?,
    cas3Application: Cas3Application? = null,
    dutyToRefer: DutyToRefer? = null,
    crs: Crs? = null,
  ) : this(
    crn = crn,
    tier = tier.tierScore,
    sex = cpr.sex?.code ?: error("Sex must not be null for DomainData"),
    releaseDate = prisonerData.mapNotNull { it.releaseDate }
      .maxByOrNull { it },
    currentAccommodation = currentAccommodation,
    nextAccommodation = proposedAddresses?.firstOrNull(),
    cas1Application = cas1Application,
    cas2CourtBailApplication = cas2CourtBailApplication,
    cas2PrisonBailApplication = cas2PrisonBailApplication,
    cas2HdcApplication = cas2HdcApplication,
    cas3Application = cas3Application,
    dtrStatus = dutyToRefer?.status,
    crsStatus = crs?.status,
  )
}
