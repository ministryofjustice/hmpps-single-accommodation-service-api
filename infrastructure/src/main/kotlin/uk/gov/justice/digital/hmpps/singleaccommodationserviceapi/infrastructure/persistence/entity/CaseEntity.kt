package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import java.util.UUID

@Entity
@Table(name = "sas_case")
data class CaseEntity(
  @Id
  val id: UUID,
  val crn: String,
  val tier: TierScore?,
  val cas1ApplicationId: UUID?,
  val cas1ApplicationApplicationStatus: Cas1ApplicationStatus?,
  val cas1ApplicationRequestForPlacementStatus: Cas1RequestForPlacementStatus?,
  val cas1ApplicationPlacementStatus: Cas1PlacementStatus?,
)
