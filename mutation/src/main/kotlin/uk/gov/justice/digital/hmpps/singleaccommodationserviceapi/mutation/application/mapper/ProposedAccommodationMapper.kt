package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate.ProposedAccommodationSnapshot
import java.time.Instant
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementType as EntityAccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementSubType as EntityAccommodationArrangementSubType
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
  )

  fun applyToEntity(snapshot: ProposedAccommodationSnapshot, entity: ProposedAccommodationEntity) {
    entity.name = snapshot.name
    entity.arrangementType = EntityAccommodationArrangementType.valueOf(snapshot.arrangementType.name)
    entity.arrangementSubType =
      snapshot.arrangementSubType?.let { EntityAccommodationArrangementSubType.valueOf(it.name) }
    entity.arrangementSubTypeDescription = snapshot.arrangementSubTypeDescription
    entity.settledType = EntityAccommodationSettledType.valueOf(snapshot.settledType.name)
    entity.verificationStatus = EntityVerificationStatus.valueOf(snapshot.verificationStatus.name)
    entity.nextAccommodationStatus = EntityNextAccommodationStatus.valueOf(snapshot.nextAccommodationStatus.name)
    entity.offenderReleaseType =
      snapshot.offenderReleaseType?.let { EntityOffenderReleaseType.valueOf(snapshot.offenderReleaseType.name) }
    entity.startDate = snapshot.startDate
    entity.endDate = snapshot.endDate
    entity.postcode = snapshot.address.postcode
    entity.subBuildingName = snapshot.address.subBuildingName
    entity.buildingName = snapshot.address.buildingName
    entity.buildingNumber = snapshot.address.buildingNumber
    entity.throughfareName = snapshot.address.thoroughfareName
    entity.dependentLocality = snapshot.address.dependentLocality
    entity.postTown = snapshot.address.postTown
    entity.county = snapshot.address.county
    entity.country = snapshot.address.country
    entity.uprn = snapshot.address.uprn
  }

  fun toAggregate(entity: ProposedAccommodationEntity): ProposedAccommodationAggregate =
    ProposedAccommodationAggregate.hydrateExisting(
      id = entity.id,
      crn = entity.crn,
      name = entity.name,
      arrangementType = AccommodationArrangementType.valueOf(entity.arrangementType.name),
      arrangementSubType = entity.arrangementSubType?.let { AccommodationArrangementSubType.valueOf(it.name) },
      arrangementSubTypeDescription = entity.arrangementSubTypeDescription,
      settledType = AccommodationSettledType.valueOf(entity.settledType.name),
      verificationStatus = VerificationStatus.valueOf(entity.verificationStatus!!.name),
      nextAccommodationStatus = NextAccommodationStatus.valueOf(entity.nextAccommodationStatus!!.name),
      offenderReleaseType = entity.offenderReleaseType?.let { OffenderReleaseType.valueOf(it.name) },
      address = AccommodationAddressDetails(
        postcode = entity.postcode,
        subBuildingName = entity.subBuildingName,
        buildingName = entity.buildingName,
        buildingNumber = entity.buildingNumber,
        thoroughfareName = entity.throughfareName,
        dependentLocality = entity.dependentLocality,
        postTown = entity.postTown,
        county = entity.county,
        country = entity.country,
        uprn = entity.uprn,
      ),
      startDate = entity.startDate,
      endDate = entity.endDate,
    )

  fun toDto(
    snapshot: ProposedAccommodationSnapshot,
    createdBy: String,
    createdAt: Instant
  ) = AccommodationDetail(
    id = snapshot.id,
    crn = snapshot.crn,
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
    createdBy = createdBy,
    createdAt = createdAt,
  )
}
