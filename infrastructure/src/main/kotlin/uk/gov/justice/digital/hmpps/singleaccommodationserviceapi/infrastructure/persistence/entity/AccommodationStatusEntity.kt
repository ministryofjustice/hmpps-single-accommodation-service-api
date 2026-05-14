package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.UUID

@Entity
@Table(name = "accommodation_status")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
data class AccommodationStatusEntity(
  @Id
  override val id: UUID,
  override val name: String,
  @Column(name = "status_code")
  override val code: String,
  val active: Boolean,
) : ReferenceData
