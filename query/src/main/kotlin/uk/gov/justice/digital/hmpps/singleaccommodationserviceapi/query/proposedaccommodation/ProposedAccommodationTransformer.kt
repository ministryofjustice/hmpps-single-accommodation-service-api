package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementSubType as EntityArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationArrangementType as EntityArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType as EntitySettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.VerificationStatus as EntityVerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.NextAccommodationStatus as EntityNextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OffenderReleaseType as EntityReleaseType

object ProposedAccommodationTransformer {
  fun toAccommodationDetails(entities: List<ProposedAccommodationEntity>): List<AccommodationDetail> =
    entities.map { toAccommodationDetail(it) }

  fun toAccommodationDetail(entity: ProposedAccommodationEntity): AccommodationDetail =
    AccommodationDetail(
      id = entity.id,
      name = entity.name,
      arrangementType = toArrangementType(entity.arrangementType),
      arrangementSubType = entity.arrangementSubType?.let { toArrangementSubType(it) },
      arrangementSubTypeDescription = entity.arrangementSubTypeDescription,
      settledType = toSettledType(entity.settledType),
      offenderReleaseType = entity.offenderReleaseType?.let { toOffenderReleaseType(it) },
      verificationStatus = entity.verificationStatus?.let { toVerificationStatus(it) },
      nextAccommodationStatus = entity.nextAccommodationStatus?.let { toNextAccommodationStatus(it) },
      address = toAddressDetails(entity),
      startDate = entity.startDate,
      endDate = entity.endDate,
      createdAt = entity.createdAt,
    )

  fun toAddressDetails(entity: ProposedAccommodationEntity): AccommodationAddressDetails =
    AccommodationAddressDetails(
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
    )

  fun toArrangementType(type: EntityArrangementType): AccommodationArrangementType =
    AccommodationArrangementType.valueOf(type.name)

  fun toArrangementSubType(subType: EntityArrangementSubType): AccommodationArrangementSubType =
    AccommodationArrangementSubType.valueOf(subType.name)

  fun toSettledType(settledType: EntitySettledType): AccommodationSettledType =
    AccommodationSettledType.valueOf(settledType.name)

  fun toVerificationStatus(status: EntityVerificationStatus): VerificationStatus =
    VerificationStatus.valueOf(status.name)

  fun toNextAccommodationStatus(status: EntityNextAccommodationStatus): NextAccommodationStatus =
    NextAccommodationStatus.valueOf(status.name)

  fun toOffenderReleaseType(releaseType: EntityReleaseType): OffenderReleaseType =
    OffenderReleaseType.valueOf(releaseType.name)
}
