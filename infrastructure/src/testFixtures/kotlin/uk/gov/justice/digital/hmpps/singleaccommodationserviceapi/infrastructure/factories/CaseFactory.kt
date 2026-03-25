package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import java.util.UUID

fun buildCaseEntity(
  id: UUID = UUID.randomUUID(),
  crn: String = "X12345",
  tier: TierScore? = null,
  cas1ApplicationId: UUID? = null,
  cas1ApplicationApplicationStatus: Cas1ApplicationStatus? = null,
  cas1ApplicationRequestForPlacementStatus: Cas1RequestForPlacementStatus? = null,
  cas1ApplicationPlacementStatus: Cas1PlacementStatus? = null,
) = CaseEntity(
  id = id,
  crn = crn,
  tier = tier,
  cas1ApplicationId = cas1ApplicationId,
  cas1ApplicationApplicationStatus = cas1ApplicationApplicationStatus,
  cas1ApplicationRequestForPlacementStatus = cas1ApplicationRequestForPlacementStatus,
  cas1ApplicationPlacementStatus = cas1ApplicationPlacementStatus,
)
