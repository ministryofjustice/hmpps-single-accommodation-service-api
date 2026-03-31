package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import java.util.UUID

class CaseAggregate private constructor(
  private val id: UUID,
  private var tierScore: TierScore? = null,
  private var cas1ApplicationId: UUID? = null,
  private var cas1ApplicationApplicationStatus: Cas1ApplicationStatus? = null,
  private var cas1ApplicationRequestForPlacementStatus: Cas1RequestForPlacementStatus? = null,
  private var cas1ApplicationPlacementStatus: Cas1PlacementStatus? = null,
) {

  fun upsertCase(
    tierScore: TierScore?,
    cas1ApplicationId: UUID?,
    cas1ApplicationApplicationStatus: Cas1ApplicationStatus?,
    cas1ApplicationRequestForPlacementStatus: Cas1RequestForPlacementStatus?,
    cas1ApplicationPlacementStatus: Cas1PlacementStatus?,
  ) {
    updateTier(tierScore)
    updateCas1ApplicationData(
      cas1ApplicationId,
      cas1ApplicationApplicationStatus,
      cas1ApplicationRequestForPlacementStatus,
      cas1ApplicationPlacementStatus,
    )
  }

  fun updateCas1ApplicationData(
    cas1ApplicationId: UUID?,
    cas1ApplicationApplicationStatus: Cas1ApplicationStatus?,
    cas1ApplicationRequestForPlacementStatus: Cas1RequestForPlacementStatus?,
    cas1ApplicationPlacementStatus: Cas1PlacementStatus?,
  ) {
    this.cas1ApplicationId = cas1ApplicationId
    this.cas1ApplicationApplicationStatus = cas1ApplicationApplicationStatus
    this.cas1ApplicationRequestForPlacementStatus = cas1ApplicationRequestForPlacementStatus
    this.cas1ApplicationPlacementStatus = cas1ApplicationPlacementStatus
  }

  companion object {
    fun hydrate(
      id: UUID,
      tierScore: TierScore?,
      cas1ApplicationId: UUID?,
      cas1ApplicationApplicationStatus: Cas1ApplicationStatus?,
      cas1ApplicationRequestForPlacementStatus: Cas1RequestForPlacementStatus?,
      cas1ApplicationPlacementStatus: Cas1PlacementStatus?,
    ) = CaseAggregate(
      id = id,
      tierScore = tierScore,
      cas1ApplicationId = cas1ApplicationId,
      cas1ApplicationApplicationStatus = cas1ApplicationApplicationStatus,
      cas1ApplicationRequestForPlacementStatus = cas1ApplicationRequestForPlacementStatus,
      cas1ApplicationPlacementStatus = cas1ApplicationPlacementStatus,
    )

    fun hydrateNew() = CaseAggregate(
      id = UUID.randomUUID(),
    )
  }

  fun updateTier(
    tierScore: TierScore?,
  ) {
    this.tierScore = tierScore
  }

  data class CaseSnapshot(
    val id: UUID,
    val tierScore: TierScore?,
    val cas1ApplicationId: UUID?,
    val cas1ApplicationApplicationStatus: Cas1ApplicationStatus?,
    val cas1ApplicationRequestForPlacementStatus: Cas1RequestForPlacementStatus?,
    val cas1ApplicationPlacementStatus: Cas1PlacementStatus?,
  )

  fun snapshot() = CaseSnapshot(
    id,
    tierScore,
    cas1ApplicationId,
    cas1ApplicationApplicationStatus,
    cas1ApplicationRequestForPlacementStatus,
    cas1ApplicationPlacementStatus,
  )
}
