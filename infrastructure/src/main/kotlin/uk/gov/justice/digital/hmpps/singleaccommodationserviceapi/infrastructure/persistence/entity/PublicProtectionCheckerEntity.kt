package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.UUID

@Entity
@Table(name = "public_protection_checker")
@EntityListeners(AuditingEntityListener::class)
class PublicProtectionCheckerEntity(
  @Id
  val id: UUID,
  val sasCaseId: UUID,
  val supervisionRequired: Boolean,
  val controlMeasuresSuitable: Boolean,
  val practicalSupportSufficient: Boolean,
)
