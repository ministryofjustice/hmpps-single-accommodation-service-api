package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationOrchestrationDto
import java.util.UUID

class CaseAggregate private constructor(
  private val id: UUID,
  private val crn: String,
  private var tier: TierScore? = null,
  private var cas1ApplicationId: UUID? = null,
  private var cas1ApplicationApplicationStatus: Cas1ApplicationStatus? = null,
  private var cas1ApplicationRequestForPlacementStatus: Cas1RequestForPlacementStatus? = null,
  private var cas1ApplicationPlacementStatus: Cas1PlacementStatus? = null,
) {

  fun upsertTier(
    newTier: TierScore,
  ) {
    tier = newTier
  }

  fun upsertCase(
    freshCase: CaseApplicationOrchestrationDto,
  ) {
    tier = freshCase.tier?.tierScore
    cas1ApplicationId = freshCase.cas1Application?.id
    cas1ApplicationApplicationStatus = freshCase.cas1Application?.applicationStatus
    cas1ApplicationRequestForPlacementStatus = freshCase.cas1Application?.requestForPlacementStatus
    cas1ApplicationPlacementStatus = freshCase.cas1Application?.placementStatus
  }

  companion object {
    fun hydrate(
      id: UUID,
      crn: String,
      tier: TierScore?,
      cas1ApplicationId: UUID?,
      cas1ApplicationApplicationStatus: Cas1ApplicationStatus?,
      cas1ApplicationRequestForPlacementStatus: Cas1RequestForPlacementStatus?,
      cas1ApplicationPlacementStatus: Cas1PlacementStatus?,
    ) = CaseAggregate(
      id = id,
      crn = crn,
      tier = tier,
      cas1ApplicationId = cas1ApplicationId,
      cas1ApplicationApplicationStatus = cas1ApplicationApplicationStatus,
      cas1ApplicationRequestForPlacementStatus = cas1ApplicationRequestForPlacementStatus,
      cas1ApplicationPlacementStatus = cas1ApplicationPlacementStatus,
    )

    fun createNew(id: UUID, crn: String) = CaseAggregate(
      id = id,
      crn = crn,
    )
  }

  data class CaseSnapshot(
    val id: UUID,
    val crn: String,
    val tier: TierScore?,
    val cas1ApplicationId: UUID?,
    val cas1ApplicationApplicationStatus: Cas1ApplicationStatus?,
    val cas1ApplicationRequestForPlacementStatus: Cas1RequestForPlacementStatus?,
    val cas1ApplicationPlacementStatus: Cas1PlacementStatus?,
  )

  fun snapshot() = CaseSnapshot(
    id,
    crn,
    tier,
    cas1ApplicationId,
    cas1ApplicationApplicationStatus,
    cas1ApplicationRequestForPlacementStatus,
    cas1ApplicationPlacementStatus,
  )
}
