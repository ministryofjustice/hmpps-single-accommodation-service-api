package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import java.util.UUID

class CaseAggregate private constructor(
  private val id: UUID,
  private var tier: TierScore? = null,
  private val caseIdentifiers: MutableList<CaseIdentifier> = mutableListOf(),
) {

  fun upsertTier(
    tier: TierScore,
  ) {
    this.tier = tier
  }

  fun addIdentifier(
    identifier: String,
    identifierType: IdentifierType,
  ) {
    val caseIdentifier = CaseIdentifier(identifier, identifierType)
    if (!caseIdentifiers.contains(caseIdentifier)) {
      caseIdentifiers.add(caseIdentifier)
    }
  }

  companion object {
    fun hydrate(
      id: UUID,
      tier: TierScore?,
      caseIdentifiers: List<CaseIdentifier>,
    ) = CaseAggregate(
      id = id,
      tier = tier,
      caseIdentifiers = caseIdentifiers.toMutableList(),
    )

    fun createNew(id: UUID = UUID.randomUUID(), identifier: String, identifierType: IdentifierType) = CaseAggregate(id = id).also { it.addIdentifier(identifier, identifierType) }
  }

  data class CaseSnapshot(
    val id: UUID,
    val tier: TierScore?,
    val caseIdentifiers: List<CaseIdentifier>,
  )

  data class CaseIdentifier(
    val identifier: String,
    val identifierType: IdentifierType,
  )

  fun snapshot() = CaseSnapshot(id, tier, caseIdentifiers.toList())
}
