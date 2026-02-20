package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

@EntityListeners(AuditingEntityListener::class)
@MappedSuperclass
open class BaseAuditedEntity {
  @CreatedBy
  @Column(name = "created_by_user_id")
  var createdByUserId: UUID? = null

  @CreatedDate
  @Column(name = "created_at")
  var createdAt: Instant? = null

  @LastModifiedBy
  @Column(name = "last_updated_by_user_id")
  var lastUpdatedByUserId: UUID? = null

  @LastModifiedDate
  @Column(name = "last_updated_at")
  var lastUpdatedAt: Instant? = null
}
