package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonDto
import java.time.LocalDate

data class DomainData(
  val crn: String,
  val tierScore: TierScore?,
  val sex: SexCode?,
  val releaseDate: LocalDate?,
  val currentAccommodationArrangementType: AccommodationArrangementType? = null,
  val hasNextAccommodation: Boolean = false,
  val cas1Application: Cas1Application? = null,
  val cas3Application: Cas3Application? = null,
  val dtrStatus: String? = "submitted",
  val crsStatus: String? = "submitted",
) {
  constructor(
    crn: String,
    cpr: CorePersonRecord,
    tier: Tier,
    prisonerData: List<Prisoner>?,
    // TODO: remove once we have a better way to determine currentAccommodationArrangementType and hasNextAccommodation
    currentAccommodation: AccommodationDetail? = null,
    nextAccommodation: AccommodationDetail? = null,
    cas1Application: Cas1Application?,
    cas3Application: Cas3Application?,
    // TODO: remove once we have a better way to determine DTR and CRS status
    dtrStatus: String? = "submitted",
    crsStatus: String? = "submitted",
  ) : this(
    crn = crn,
    tierScore = tier.tierScore,
    sex = cpr.sex?.code,
    releaseDate = prisonerData?.let { prisonerData.mapNotNull { it.releaseDate }.maxByOrNull { it } },
    currentAccommodationArrangementType = currentAccommodation?.arrangementType,
    hasNextAccommodation = nextAccommodation != null,
    cas1Application = cas1Application,
    cas3Application = cas3Application,
    dtrStatus = dtrStatus,
    crsStatus = crsStatus,
  )

  constructor(
    personDto: PersonDto,
    caseEntity: CaseEntity?,
  ) : this(
    crn = personDto.crn,
    tierScore = caseEntity?.tierScore,
    // TODO check that we are transforming the gender correctly
    sex = SexCode.findByGender(personDto.gender),
    releaseDate = personDto.expectedReleaseDate,
    cas1Application = if (caseEntity?.cas1ApplicationId != null && caseEntity.cas1ApplicationApplicationStatus != null) {
      Cas1Application(
        id = caseEntity.cas1ApplicationId!!,
        applicationStatus = caseEntity.cas1ApplicationApplicationStatus!!,
        requestForPlacementStatus = caseEntity.cas1ApplicationRequestForPlacementStatus,
        placementStatus = caseEntity.cas1ApplicationPlacementStatus,
      )
    } else {
      null
    },
    cas3Application = null,
  )
}
