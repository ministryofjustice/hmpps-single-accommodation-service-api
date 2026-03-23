package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2CourtBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2HdcApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2PrisonBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.CaseListItem
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import java.time.LocalDate
import java.util.UUID

data class DomainData(
  val crn: String,
  val tier: TierScore?,
  val sex: SexCode?,
  val releaseDate: LocalDate?,
  val currentAccommodationArrangementType: AccommodationArrangementType? = null,
  val nextAccommodationId: UUID? = null,
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
    prisonerData: List<Prisoner>?,
    currentAccommodation: AccommodationDetail? = null,
    nextAccommodation: AccommodationDetail? = null,
    cas1Application: Cas1Application?,
    cas2CourtBailApplication: Cas2CourtBailApplication?,
    cas2PrisonBailApplication: Cas2PrisonBailApplication?,
    cas2HdcApplication: Cas2HdcApplication?,
    cas3Application: Cas3Application? = null,
    dtrStatus: String? = null,
    crsStatus: String? = null,
  ) : this(
    crn = crn,
    tier = tier.tierScore,
    sex = cpr.sex?.code,
    releaseDate = prisonerData?.let { prisonerData.mapNotNull { it.releaseDate }.maxByOrNull { it } },
    currentAccommodationArrangementType = currentAccommodation?.let { AccommodationArrangementType.valueOf(currentAccommodation.arrangementType.toString()) },
    nextAccommodationId = nextAccommodation?.id,
    cas1Application = cas1Application,
    cas2CourtBailApplication = cas2CourtBailApplication,
    cas2PrisonBailApplication = cas2PrisonBailApplication,
    cas2HdcApplication = cas2HdcApplication,
    cas3Application = cas3Application,
    dtrStatus = dtrStatus,
    crsStatus = crsStatus,
  )

  constructor(
    caseEntity: CaseEntity,
    caseListItem: CaseListItem,
  ) : this(
    crn = caseEntity.crn,
    tier = caseEntity.tier,
    // TODO: handle null gender
    sex = SexCode.valueOf(caseListItem.gender),
    // TODO: handle
    releaseDate = caseListItem.expectedReleaseDate,
    currentAccommodationArrangementType = AccommodationArrangementType.valueOf(caseEntity.currentAccommodationArrangementType.toString()),
    nextAccommodationId = caseEntity.nextAccommodationId,
    cas1Application = caseEntity.cas1ApplicationId?.let {
      Cas1Application(
        id = it,
        applicationStatus = caseEntity.cas1ApplicationApplicationStatus!!,
        placementStatus = caseEntity.cas1ApplicationPlacementStatus,
        crn = caseEntity.crn,
      )
    },
    cas2CourtBailApplication = caseEntity.cas2CourtBailApplicationId?.let {
      Cas2CourtBailApplication(
        id = it,
        crn = caseEntity.crn,
      )
    },
    cas2PrisonBailApplication = caseEntity.cas2PrisonBailApplicationId?.let {
      Cas2PrisonBailApplication(
        id = it,
        crn = caseEntity.crn,
      )
    },
    cas2HdcApplication = caseEntity.cas2HdcApplicationId?.let {
      Cas2HdcApplication(
        id = it,
        crn = caseEntity.crn,
      )
    },
    cas3Application = caseEntity.cas3ApplicationId?.let {
      Cas3Application(
        id = it,
        applicationStatus = caseEntity.cas3ApplicationApplicationStatus!!,
        placementStatus = caseEntity.cas3ApplicationPlacementStatus,
        crn = caseEntity.crn,
      )
    },
    dtrStatus = caseEntity.dtrStatus,
    crsStatus = caseEntity.crsStatus,
  )
}
