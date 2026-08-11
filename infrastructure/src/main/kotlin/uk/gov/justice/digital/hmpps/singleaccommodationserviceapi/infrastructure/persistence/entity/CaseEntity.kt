package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "sas_case")
class CaseEntity(

  @Id
  val id: UUID,
  var hasSyncedCprProposedAccommodation: Boolean,
  var tierScore: String? = null,

  @OneToMany(
    mappedBy = "caseEntity",
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    orphanRemoval = true,
  )
  var caseIdentifiers: MutableSet<CaseIdentifierEntity> = mutableSetOf(),

) {
  fun latestCrn() = this.caseIdentifiers.filter { it.identifierType == IdentifierType.CRN }.maxBy { it.createdAt }
    .identifier
  fun latestPrisonNumber() = this.caseIdentifiers.filter { it.identifierType == IdentifierType.PRISON_NUMBER }.maxByOrNull { it.createdAt }
    ?.identifier
}
