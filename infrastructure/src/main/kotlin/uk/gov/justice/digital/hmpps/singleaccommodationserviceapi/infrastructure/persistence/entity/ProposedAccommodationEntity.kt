package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "proposed_accommodation")
open class ProposedAccommodationEntity(
  @Id
  val id: UUID,
  val crn: String,
  var name: String?,
  @Enumerated(EnumType.STRING)
  var arrangementType: AccommodationArrangementType,
  @Enumerated(EnumType.STRING)
  var arrangementSubType: AccommodationArrangementSubType?,
  var arrangementSubTypeDescription: String?,
  @Enumerated(EnumType.STRING)
  var settledType: AccommodationSettledType,
  @Enumerated(EnumType.STRING)
  var verificationStatus: VerificationStatus?,
  @Enumerated(EnumType.STRING)
  var nextAccommodationStatus: NextAccommodationStatus?,
  @Enumerated(EnumType.STRING)
  var offenderReleaseType: OffenderReleaseType?,
  var startDate: LocalDate?,
  var endDate: LocalDate?,
  var postcode: String?,
  var subBuildingName: String?,
  var buildingName: String?,
  var buildingNumber: String?,
  var throughfareName: String?,
  var dependentLocality: String?,
  var postTown: String?,
  var county: String?,
  var country: String?,
  var uprn: String?,
) : BaseAuditedEntity()

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
  PASSED,
}

enum class NextAccommodationStatus {
  YES,
  NO,
  TO_BE_DECIDED,
}

enum class OffenderReleaseType {
  REMAND,
  LICENCE,
  BAIL,
}
