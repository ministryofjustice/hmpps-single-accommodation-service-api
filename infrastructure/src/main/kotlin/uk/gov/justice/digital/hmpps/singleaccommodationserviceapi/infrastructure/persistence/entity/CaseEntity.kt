package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import java.util.UUID

@Entity
@Table(name = "sas_case")
class CaseEntity(

  @Id
  val id: UUID,

  @Enumerated(EnumType.STRING)
  var tierScore: TierScore? = null,

  var cas1ApplicationId: UUID?,
  @Enumerated(EnumType.STRING)
  var cas1ApplicationApplicationStatus: Cas1ApplicationStatus?,
  @Enumerated(EnumType.STRING)
  var cas1ApplicationRequestForPlacementStatus: Cas1RequestForPlacementStatus?,
  @Enumerated(EnumType.STRING)
  var cas1ApplicationPlacementStatus: Cas1PlacementStatus?,

  @OneToMany(
    mappedBy = "caseEntity",
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    orphanRemoval = true,
  )
  var caseIdentifiers: MutableSet<CaseIdentifierEntity> = mutableSetOf(),

)
