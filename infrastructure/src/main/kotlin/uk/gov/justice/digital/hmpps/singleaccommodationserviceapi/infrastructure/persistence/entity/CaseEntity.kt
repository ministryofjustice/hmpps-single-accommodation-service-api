package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import java.util.UUID

@Entity
@Table(name = "sas_case")
data class CaseEntity(
  @Id
  val id: UUID,
  val crn: String,
  val tier: TierScore?,
)
