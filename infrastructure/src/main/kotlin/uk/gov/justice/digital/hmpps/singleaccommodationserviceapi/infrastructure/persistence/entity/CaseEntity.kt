package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import java.util.UUID

@Entity
@Table(name = "sas_case")
class CaseEntity(

  @Id
  val id: UUID,
  val tier: TierScore?,

) {

  @OneToMany(
    mappedBy = "caseEntity",
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    orphanRemoval = true,
  )
  private val _caseIdentifiers: MutableSet<CaseIdentifierEntity> = mutableSetOf()

  val caseIdentifiers: Set<CaseIdentifierEntity>
    get() = _caseIdentifiers.toSet()

  fun addIdentifier(
    identifier: String,
    identifierType: IdentifierType,
  ) {
    if (_caseIdentifiers.none { it.identifierType == identifierType && it.identifier == identifier }) {
      _caseIdentifiers.add(
        CaseIdentifierEntity(
          caseEntity = this,
          identifier = identifier,
          identifierType = identifierType,
        ),
      )
    }
  }
}
