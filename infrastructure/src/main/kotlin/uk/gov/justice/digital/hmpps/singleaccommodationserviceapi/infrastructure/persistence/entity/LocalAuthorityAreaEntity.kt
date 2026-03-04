package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ReferenceData
import java.util.UUID

@Entity
@Table(name = "local_authority_area")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@Immutable
data class LocalAuthorityAreaEntity(
  @Id
  override val id: UUID,
  val identifier: String,
  override val name: String,
  val active: Boolean,
) : ReferenceData {
  @Transient
  override val description: String? = null
}
