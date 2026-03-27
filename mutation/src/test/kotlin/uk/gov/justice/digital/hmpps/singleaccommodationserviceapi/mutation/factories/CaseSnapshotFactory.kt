package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate.CaseSnapshot
import java.util.UUID

fun buildCaseSnapshot(
  tier: TierScore? = null,
  cas1ApplicationId: UUID? = null,
  cas1ApplicationApplicationStatus: Cas1ApplicationStatus? = null,
  cas1ApplicationRequestForPlacementStatus: Cas1RequestForPlacementStatus? = null,
  cas1ApplicationPlacementStatus: Cas1PlacementStatus? = null,
) = CaseSnapshot(
  id = UUID.randomUUID(),
  tier = tier,
  cas1ApplicationId = cas1ApplicationId,
  cas1ApplicationApplicationStatus = cas1ApplicationApplicationStatus,
  cas1ApplicationRequestForPlacementStatus = cas1ApplicationRequestForPlacementStatus,
  cas1ApplicationPlacementStatus = cas1ApplicationPlacementStatus,
)
