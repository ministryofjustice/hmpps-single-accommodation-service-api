package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import java.util.UUID

class CaseAggregate private constructor(
  private val id: UUID,
  private var tierScore: TierScore? = null,
  private val caseIdentifiers: MutableSet<CaseIdentifier> = mutableSetOf(),
) {
  companion object {
    fun hydrate(
      id: UUID,
      tierScore: TierScore?,
      caseIdentifiers: MutableSet<CaseIdentifier>,
    ) = CaseAggregate(
      id = id,
      tierScore = tierScore,
      caseIdentifiers = caseIdentifiers,
    )

    fun createNew(id: UUID, caseIdentifiers: MutableSet<CaseIdentifier>) = CaseAggregate(
      id = id,
      caseIdentifiers = caseIdentifiers,
    )
  }

  fun updateTier(
    tierScore: TierScore,
  ) {
    this.tierScore = tierScore
  }

  data class CaseSnapshot(
    val id: UUID,
    val caseIdentifiers: Set<CaseIdentifier>,
    val tierScore: TierScore?,
  )

  data class CaseIdentifier(
    val id: UUID,
    val identifier: String,
    val identifierType: IdentifierType,
  )

  fun snapshot() = CaseSnapshot(id, caseIdentifiers, tierScore)
}
