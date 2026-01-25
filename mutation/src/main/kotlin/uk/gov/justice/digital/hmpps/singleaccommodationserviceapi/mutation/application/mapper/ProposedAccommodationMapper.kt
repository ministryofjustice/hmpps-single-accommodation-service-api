package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate.ProposedAccommodationSnapshot
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.AccommodationArrangementSettledTypeMapper.toAccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.AccommodationStatusMapper.toAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.OffenderReleaseTypeMapper.toOffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.ProposedAccommodationArrangementSubTypeMapper.toAccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.ProposedAccommodationArrangementTypeMapper.toAccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementType as AccommodationArrangementTypeInfra
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementSubType as AccommodationArrangementSubTypeInfra
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType as AccommodationSettledTypeInfra
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationStatus as AccommodationStatusInfra
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OffenderReleaseType as OffenderReleaseTypeInfra

object ProposedAccommodationMapper {

  fun toEntity(snapshot: ProposedAccommodationSnapshot) = ProposedAccommodationEntity(
    id = snapshot.id,
    crn = snapshot.crn,
    name = snapshot.name,
    arrangementType = toAccommodationArrangementType(snapshot.arrangementType),
    arrangementSubType = toAccommodationArrangementSubType(snapshot.arrangementSubType),
    arrangementSubTypeDescription = snapshot.arrangementSubTypeDescription,
    settledType = toAccommodationSettledType(snapshot.settledType),
    status = toAccommodationStatus(snapshot.status),
    offenderReleaseType = toOffenderReleaseType(snapshot.offenderReleaseType),
    startDate = snapshot.startDate,
    endDate = snapshot.endDate,
    postcode = snapshot.address.postcode,
    subBuildingName = snapshot.address.subBuildingName,
    buildingName = snapshot.address.buildingName,
    buildingNumber = snapshot.address.buildingNumber,
    throughfareName = snapshot.address.thoroughfareName,
    dependentLocality = snapshot.address.dependentLocality,
    postTown = snapshot.address.postTown,
    county = snapshot.address.county,
    country = snapshot.address.country,
    uprn = snapshot.address.uprn,
    createdAt = snapshot.createdAt,
    lastUpdatedAt = snapshot.lastUpdatedAt,
  )

  fun toDto(snapshot: ProposedAccommodationSnapshot) = AccommodationDetail(
    id = snapshot.id,
    crn = snapshot.crn,
    name = snapshot.name,
    arrangementType = snapshot.arrangementType,
    arrangementSubType = snapshot.arrangementSubType,
    arrangementSubTypeDescription = snapshot.arrangementSubTypeDescription,
    settledType = snapshot.settledType,
    status = snapshot.status,
    startDate = snapshot.startDate,
    endDate = snapshot.endDate,
    offenderReleaseType = snapshot.offenderReleaseType,
    address = snapshot.address,
    createdAt = snapshot.createdAt,
  )
}

object ProposedAccommodationArrangementTypeMapper {
  fun toAccommodationArrangementType(accommodationArrangementType: AccommodationArrangementType) =
    AccommodationArrangementTypeInfra.valueOf(accommodationArrangementType.name)
}

object ProposedAccommodationArrangementSubTypeMapper {
  fun toAccommodationArrangementSubType(accommodationArrangementSubType: AccommodationArrangementSubType?) =
    accommodationArrangementSubType?.let { AccommodationArrangementSubTypeInfra.valueOf(accommodationArrangementSubType.name) }
}

object AccommodationArrangementSettledTypeMapper {
  fun toAccommodationSettledType(accommodationSettledType: AccommodationSettledType) =
    AccommodationSettledTypeInfra.valueOf(accommodationSettledType.name)
}

object AccommodationStatusMapper {
  fun toAccommodationStatus(accommodationStatus: AccommodationStatus) =
    AccommodationStatusInfra.valueOf(accommodationStatus.name)
}

object OffenderReleaseTypeMapper {
  fun toOffenderReleaseType(offenderReleaseType: OffenderReleaseType?) =
    offenderReleaseType?.let { OffenderReleaseTypeInfra.valueOf(offenderReleaseType.name) }
}
