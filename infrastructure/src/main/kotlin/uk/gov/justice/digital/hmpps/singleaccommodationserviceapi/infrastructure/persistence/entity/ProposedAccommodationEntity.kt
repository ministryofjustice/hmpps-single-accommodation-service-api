package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "proposed_accommodation")
data class ProposedAccommodationEntity(
  @Id
  val id: UUID,
  val crn: String,
  val name: String?,
  val arrangementType: AccommodationArrangementType,
  val arrangementSubType: AccommodationArrangementSubType?,
  val arrangementSubTypeDescription: String?,
  val settledType: AccommodationSettledType,
  val status: AccommodationStatus?,
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
  var createdAt: Instant,
  var lastUpdatedAt: Instant?,
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

enum class AccommodationStatus {
  NOT_CHECKED_YET,
  CHECKS_PASSED,
  CHECKS_FAILED,
  CONFIRMED
}

enum class OffenderReleaseType {
  REMAND,
  LICENCE,
  BAIL,
}
