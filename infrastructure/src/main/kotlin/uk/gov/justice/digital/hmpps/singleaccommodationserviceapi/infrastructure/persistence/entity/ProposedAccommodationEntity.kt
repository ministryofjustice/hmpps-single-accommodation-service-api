package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "proposed_accommodation")
@EntityListeners(AuditingEntityListener::class)
data class ProposedAccommodationEntity(
  @Id
  val id: UUID,
  val crn: String,
  val name: String?,
  @Enumerated(EnumType.STRING)
  val arrangementType: AccommodationArrangementType,
  @Enumerated(EnumType.STRING)
  val arrangementSubType: AccommodationArrangementSubType?,
  val arrangementSubTypeDescription: String?,
  @Enumerated(EnumType.STRING)
  val settledType: AccommodationSettledType,
  @Enumerated(EnumType.STRING)
  val verificationStatus: VerificationStatus?,
  @Enumerated(EnumType.STRING)
  val nextAccommodationStatus: NextAccommodationStatus?,
  val offenderReleaseType: OffenderReleaseType?,
  val startDate: LocalDate?,
  val endDate: LocalDate?,
  val postcode: String?,
  val subBuildingName: String?,
  val buildingName: String?,
  val buildingNumber: String?,
  val throughfareName: String?,
  val dependentLocality: String?,
  val postTown: String?,
  val county: String?,
  val country: String?,
  val uprn: String?,

  @CreatedBy
  @Column(name = "created_by_user_id")
  var createdByUserId: UUID? = null,
  @CreatedDate
  var createdAt: Instant? = null,
  @LastModifiedBy
  @Column(name = "last_updated_by_user_id")
  var lastUpdatedByUserId: UUID? = null,
  @LastModifiedDate
  @Column(name = "last_updated_at")
  var lastUpdatedAt: Instant? = null
)

enum class AccommodationArrangementType {
  PRISON,
  CAS1,
  CAS2,
  CAS2V2,
  CAS3,
  PRIVATE,
  NO_FIXED_ABODE,
}

enum class AccommodationArrangementSubType {
  FRIENDS_OR_FAMILY,
  SOCIAL_RENTED,
  PRIVATE_RENTED_WHOLE_PROPERTY,
  PRIVATE_RENTED_ROOM,
  OWNED,
  OTHER,
}

enum class AccommodationSettledType {
  SETTLED,
  TRANSIENT,
}

enum class VerificationStatus {
  NOT_CHECKED_YET,
  FAILED,
  PASSED
}

enum class NextAccommodationStatus {
  YES,
  NO,
  TO_BE_DECIDED
}

enum class OffenderReleaseType {
  REMAND,
  LICENCE,
  BAIL,
}
