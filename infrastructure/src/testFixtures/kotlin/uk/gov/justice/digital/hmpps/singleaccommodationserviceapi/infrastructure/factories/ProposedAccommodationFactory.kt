package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun buildProposedAccommodationEntity(
  id: UUID = UUID.randomUUID(),
  crn: String = "X12345",
  name: String? = "Test Accommodation",
  arrangementType: AccommodationArrangementType = AccommodationArrangementType.PRIVATE,
  arrangementSubType: AccommodationArrangementSubType? = AccommodationArrangementSubType.FRIENDS_OR_FAMILY,
  arrangementSubTypeDescription: String? = null,
  settledType: AccommodationSettledType = AccommodationSettledType.SETTLED,
  status: AccommodationStatus? = AccommodationStatus.NOT_CHECKED_YET,
  offenderReleaseType: OffenderReleaseType? = null,
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
  createdAt: Instant = Instant.now(),
  lastUpdatedAt: Instant? = null,
) = ProposedAccommodationEntity(
  id = id,
  crn = crn,
  name = name,
  arrangementType = arrangementType,
  arrangementSubType = arrangementSubType,
  arrangementSubTypeDescription = arrangementSubTypeDescription,
  settledType = settledType,
  status = status,
  offenderReleaseType = offenderReleaseType,
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
  createdAt = createdAt,
  lastUpdatedAt = lastUpdatedAt,
)
