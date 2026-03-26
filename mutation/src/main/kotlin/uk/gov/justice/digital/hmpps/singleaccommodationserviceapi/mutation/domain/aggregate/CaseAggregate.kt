package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import java.util.UUID

class CaseAggregate private constructor(
  private val id: UUID,
  private var tier: TierScore? = null,
  private var cas1ApplicationId: UUID? = null,
  private var cas1ApplicationApplicationStatus: Cas1ApplicationStatus? = null,
  private var cas1ApplicationRequestForPlacementStatus: Cas1RequestForPlacementStatus? = null,
  private var cas1ApplicationPlacementStatus: Cas1PlacementStatus? = null,
  private val caseIdentifiers: MutableSet<CaseIdentifier> = mutableSetOf(),
) {
  companion object {
    fun hydrate(
      id: UUID,
      tierScore: TierScore?,
      caseIdentifiers: MutableSet<CaseIdentifier>,
    ) = CaseAggregate(
      id = id,
      tier = tierScore,
      caseIdentifiers = caseIdentifiers,
    )

    fun createNew(id: UUID, caseIdentifiers: MutableSet<CaseIdentifier>) = CaseAggregate(
      id = id,
      caseIdentifiers = caseIdentifiers,
    )
  }

  fun upsertCase(
    tier: TierScore?,
    cas1ApplicationId: UUID?,
    cas1ApplicationApplicationStatus: Cas1ApplicationStatus?,
    cas1ApplicationRequestForPlacementStatus: Cas1RequestForPlacementStatus?,
    cas1ApplicationPlacementStatus: Cas1PlacementStatus?,
  ) {
    updateTier(tier)
    updateCas1ApplicationData(
      cas1ApplicationId,
      cas1ApplicationApplicationStatus,
      cas1ApplicationRequestForPlacementStatus,
      cas1ApplicationPlacementStatus,
    )
  }

  fun updateTier(
    tierScore: TierScore?,
  ) {
    this.tier = tierScore
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

  data class CaseSnapshot(
    val id: UUID,
    val caseIdentifiers: Set<CaseIdentifier>,
    val tier: TierScore?,
  )

  data class CaseIdentifier(
    val id: UUID,
    val identifier: String,
    val identifierType: IdentifierType,
  )

  fun snapshot() = CaseSnapshot(id, caseIdentifiers, tier)
}
