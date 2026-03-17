package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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
  @Enumerated(EnumType.STRING)
  @Column(name = "tier")
  var tierScore: TierScore? = null,

  @OneToMany(
    mappedBy = "caseEntity",
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    orphanRemoval = true,
  )
  var caseIdentifiers: MutableSet<CaseIdentifierEntity> = mutableSetOf(),

)
