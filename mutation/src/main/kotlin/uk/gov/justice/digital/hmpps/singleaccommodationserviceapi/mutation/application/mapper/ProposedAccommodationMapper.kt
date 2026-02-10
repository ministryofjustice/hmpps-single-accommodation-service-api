package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate.ProposedAccommodationSnapshot
import java.time.temporal.ChronoUnit
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementSubType as EntityAccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementType as EntityAccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType as EntityAccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.NextAccommodationStatus as EntityNextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OffenderReleaseType as EntityOffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.VerificationStatus as EntityVerificationStatus

object ProposedAccommodationMapper {

  fun toEntity(snapshot: ProposedAccommodationSnapshot) = ProposedAccommodationEntity(
    id = snapshot.id,
    crn = snapshot.crn,
    name = snapshot.name,
    arrangementType = EntityAccommodationArrangementType.valueOf(snapshot.arrangementType.name),
    arrangementSubType = snapshot.arrangementSubType?.let { EntityAccommodationArrangementSubType.valueOf(it.name) },
    arrangementSubTypeDescription = snapshot.arrangementSubTypeDescription,
    settledType = EntityAccommodationSettledType.valueOf(snapshot.settledType.name),
    verificationStatus = EntityVerificationStatus.valueOf(snapshot.verificationStatus.name),
    nextAccommodationStatus = EntityNextAccommodationStatus.valueOf(snapshot.nextAccommodationStatus.name),
    offenderReleaseType = snapshot.offenderReleaseType?.let { EntityOffenderReleaseType.valueOf(snapshot.offenderReleaseType.name) },
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
    name = snapshot.name,
    arrangementType = snapshot.arrangementType,
    arrangementSubType = snapshot.arrangementSubType,
    arrangementSubTypeDescription = snapshot.arrangementSubTypeDescription,
    settledType = snapshot.settledType,
    verificationStatus = snapshot.verificationStatus,
    nextAccommodationStatus = snapshot.nextAccommodationStatus,
    startDate = snapshot.startDate,
    endDate = snapshot.endDate,
    offenderReleaseType = snapshot.offenderReleaseType,
    address = snapshot.address,
    createdAt = snapshot.createdAt.truncatedTo(ChronoUnit.SECONDS),
  )
}
