package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationStatusEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.TestData
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@TestData
fun buildProposedAccommodationEntity(
  id: UUID = UUID.randomUUID(),
  caseId: UUID = UUID.randomUUID(),
  cprAddressId: UUID? = null,
  name: String? = "Test Accommodation",
  accommodationTypeEntity: AccommodationTypeEntity = buildAccommodationTypeEntity(),
  accommodationStatusEntity: AccommodationStatusEntity? = buildAccommodationStatusEntity(),
  verificationStatus: VerificationStatus? = VerificationStatus.NOT_CHECKED_YET,
  nextAccommodationStatus: NextAccommodationStatus? = NextAccommodationStatus.TO_BE_DECIDED,
  startDate: LocalDate? = null,
  endDate: LocalDate? = null,
  postcode: String? = "SW1A 1AA",
  subBuildingName: String? = null,
  buildingName: String? = null,
  buildingNumber: String? = "10",
  throughfareName: String? = "Downing Street",
  dependentLocality: String? = null,
  postTown: String? = "London",
  county: String? = null,
  country: String? = "England",
  uprn: String? = null,
  createdByUserId: UUID? = null,
  createdAt: Instant = Instant.now(),
  lastUpdatedByUserId: UUID? = null,
  lastUpdatedAt: Instant = Instant.now(),
) = ProposedAccommodationEntity(
  id = id,
  caseId = caseId,
  cprAddressId = cprAddressId,
  name = name,
  accommodationTypeId = accommodationTypeEntity.id,
  accommodationStatusId = accommodationStatusEntity?.id,
  verificationStatus = verificationStatus,
  nextAccommodationStatus = nextAccommodationStatus,
  startDate = startDate,
  endDate = endDate,
  postcode = postcode,
  subBuildingName = subBuildingName,
  buildingName = buildingName,
  buildingNumber = buildingNumber,
  throughfareName = throughfareName,
  dependentLocality = dependentLocality,
  postTown = postTown,
  county = county,
  country = country,
  uprn = uprn,
).apply {
  this.createdByUserId = createdByUserId
  this.createdAt = createdAt
  this.lastUpdatedByUserId = lastUpdatedByUserId
  this.lastUpdatedAt = lastUpdatedAt
}

@TestData
fun buildProposedAccommodationNoteEntity(
  id: UUID = UUID.randomUUID(),
  note: String = "Test note",
  createdByUserId: UUID? = UUID.randomUUID(),
  createdAt: Instant = Instant.now(),
  proposedAccommodationEntity: ProposedAccommodationEntity,
) = ProposedAccommodationNoteEntity(
  id,
  note,
  proposedAccommodationEntity,
).apply {
  this.createdByUserId = createdByUserId
  this.createdAt = createdAt
}

@TestData
fun buildAccommodationTypeEntity(
  id: UUID = UUID.randomUUID(),
  code: String = "A07B",
  name: String = "Living in the home of a friend, family member or partner: settled",
  settledType: AccommodationSettledType = AccommodationSettledType.SETTLED,
  active: Boolean = true,
  isProposed: Boolean = false,
  isPrivate: Boolean = false,
  isPrison: Boolean = false,
  isCas1: Boolean = false,
  isCas2: Boolean = false,
) = AccommodationTypeEntity(
  id,
  name = name,
  code = code,
  settledType,
  active,
  isProposed = isProposed,
  isPrivate = isPrivate,
  isPrison = isPrison,
  isCas1 = isCas1,
  isCas2 = isCas2,
)

@TestData
fun buildAccommodationStatusEntity(
  id: UUID = UUID.randomUUID(),
  code: String = "M",
  name: String = "Main",
  active: Boolean = true,
) = AccommodationStatusEntity(
  id,
  name = name,
  code = code,
  active,
)
