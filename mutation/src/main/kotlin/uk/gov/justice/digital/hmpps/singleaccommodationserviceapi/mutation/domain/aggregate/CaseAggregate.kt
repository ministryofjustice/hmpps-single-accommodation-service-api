package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import java.util.UUID

class CaseAggregate private constructor(
  private val id: UUID,
  private var tier: TierScore? = null,
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

  fun updateTier(
    tierScore: TierScore,
  ) {
    this.tier = tierScore
  }

  fun updateIdentifiers(
    identifiers: Set<Pair<String, IdentifierType>>,
  ) {
    val existingIdentifiers = this.caseIdentifiers.map { it.identifier to it.identifierType }.toSet()
    identifiers.forEach { identifier ->
      if (!existingIdentifiers.contains(identifier)) {
        caseIdentifiers.add(
          CaseIdentifier(
            id = UUID.randomUUID(),
            identifier = identifier.first,
            identifierType = identifier.second,
          ),
        )
      }
    }
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
