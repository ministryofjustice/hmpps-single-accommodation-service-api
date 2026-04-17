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

  @OneToMany(
    mappedBy = "caseEntity",
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    orphanRemoval = true,
  )
  var caseIdentifiers: MutableSet<CaseIdentifierEntity> = mutableSetOf(),
  var cas1ApplicationId: UUID? = null,
  @Enumerated(EnumType.STRING)
  var cas1ApplicationApplicationStatus: Cas1ApplicationStatus? = null,
  @Enumerated(EnumType.STRING)
  var cas1ApplicationRequestForPlacementStatus: Cas1RequestForPlacementStatus? = null,
  @Enumerated(EnumType.STRING)
  var cas1ApplicationPlacementStatus: Cas1PlacementStatus? = null,

) {

  // TODO we need a better and more reliable way to work out the latest crn.
  //  This won't work (depending or order of crns we get from cpr, order we add them to our db etc

  fun latestCrn() = this.caseIdentifiers.filter { it.identifierType == IdentifierType.CRN }.maxBy { it.createdAt }
    .identifier
}
