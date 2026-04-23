package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonDto
import java.time.LocalDate

data class DomainData(
  val crn: String,
  val tierScore: TierScore?,
  val sex: SexCode?,
  val currentAccommodation: CurrentAccommodation?,
  val hasNextAccommodation: Boolean,
  val cas1Application: Cas1Application?,
  val cas3Application: Cas3Application?,
  val dtrStatus: String? = "submitted",
  val crsStatus: String? = "submitted",
) {
  constructor(
    crn: String,
    cpr: CorePersonRecord?,
    tier: Tier?,
    currentAccommodationSummary: AccommodationSummaryDto?,
    cas1Application: Cas1Application?,
    cas3Application: Cas3Application?,
    // TODO: remove once we have a better way to determine DTR and CRS status
    dtrStatus: String? = "submitted",
    crsStatus: String? = "submitted",
  ) : this(
    crn = crn,
    tierScore = tier?.tierScore,
    sex = cpr?.sex?.code,
    currentAccommodation = currentAccommodationSummary?.let {
      CurrentAccommodation(
        endDate = it.endDate,
        isPrisonCas1Cas2OrCas2v2 = true,
      )
    },
    hasNextAccommodation = false,
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
    sex = SexCode.findByGender(personDto.gender),
    currentAccommodation = null,
    hasNextAccommodation = false,
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

data class CurrentAccommodation(
  val endDate: LocalDate? = null,
  val isPrisonCas1Cas2OrCas2v2: Boolean = true,
)
