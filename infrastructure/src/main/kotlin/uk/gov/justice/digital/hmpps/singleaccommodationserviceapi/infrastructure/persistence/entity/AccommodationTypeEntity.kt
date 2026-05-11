package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.UUID

@Entity
@Table(name = "accommodation_type")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
data class AccommodationTypeEntity(
  @Id
  override val id: UUID,
  override val name: String,
  @Column(name = "type_code")
  override val code: String,
  @Enumerated(EnumType.STRING)
  val settledType: AccommodationSettledType,
  val active: Boolean,
  val isProposed: Boolean,
  val isPrivate: Boolean,
  val isPrison: Boolean,
  val isCas1: Boolean,
  val isCas2: Boolean,
) : ReferenceData

enum class AccommodationSettledType {
  SETTLED,
  TRANSIENT,
}
