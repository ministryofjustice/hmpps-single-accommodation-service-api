package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import java.util.UUID

fun buildCaseEntity(
  id: UUID = UUID.randomUUID(),
  crn: String = "X12345",
  tier: TierScore = TierScore.A1,
  currentAccommodationArrangementType: AccommodationArrangementType? = null,
  nextAccommodationId: UUID? = null,
  crsStatus: String? = null,
  dtrStatus: String? = null,
  cas3ApplicationPlacementStatus: Cas3PlacementStatus? = null,
  cas3ApplicationApplicationStatus: Cas3ApplicationStatus? = null,
  cas3ApplicationId: UUID? = null,
  cas2HdcApplicationId: UUID? = null,
  cas2PrisonBailApplicationId: UUID? = null,
  cas2CourtBailApplicationId: UUID? = null,
  cas1ApplicationPlacementStatus: Cas1PlacementStatus? = null,
  cas1ApplicationApplicationStatus: Cas1ApplicationStatus? = null,
  cas1ApplicationId: UUID? = null,
) = CaseEntity(
  id = id,
  crn = crn,
  tier = tier,
  currentAccommodationArrangementType = currentAccommodationArrangementType,
  nextAccommodationId = nextAccommodationId,
  crsStatus = crsStatus,
  dtrStatus = dtrStatus,
  cas3ApplicationPlacementStatus = cas3ApplicationPlacementStatus,
  cas3ApplicationApplicationStatus = cas3ApplicationApplicationStatus,
  cas3ApplicationId = cas3ApplicationId,
  cas2HdcApplicationId = cas2HdcApplicationId,
  cas2PrisonBailApplicationId = cas2PrisonBailApplicationId,
  cas2CourtBailApplicationId = cas2CourtBailApplicationId,
  cas1ApplicationPlacementStatus = cas1ApplicationPlacementStatus,
  cas1ApplicationApplicationStatus = cas1ApplicationApplicationStatus,
  cas1ApplicationId = cas1ApplicationId,
)
