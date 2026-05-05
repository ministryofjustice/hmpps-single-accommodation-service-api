package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ProposedAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.NextAccommodationStatus as EntityNextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.VerificationStatus as EntityVerificationStatus

object ProposedAccommodationTransformer {

  fun toAccommodationDetail(
    proposedAccommodationEntity: ProposedAccommodationEntity,
    accommodationTypeEntity: AccommodationTypeEntity,
    crn: String,
    createdBy: String,
  ) = ProposedAccommodationDto(
    id = proposedAccommodationEntity.id,
    name = proposedAccommodationEntity.name,
    crn = crn,
    accommodationType = AccommodationTypeDto(
      code = accommodationTypeEntity.code,
      description = accommodationTypeEntity.name,
    ),
    verificationStatus = proposedAccommodationEntity.verificationStatus?.let { toVerificationStatus(it) },
    nextAccommodationStatus = proposedAccommodationEntity.nextAccommodationStatus?.let { toNextAccommodationStatus(it) },
    address = toAddressDetails(proposedAccommodationEntity),
    startDate = proposedAccommodationEntity.startDate,
    endDate = proposedAccommodationEntity.endDate,
    createdBy = createdBy,
    createdAt = proposedAccommodationEntity.createdAt!!,
  )

  fun toAddressDetails(entity: ProposedAccommodationEntity): AccommodationAddressDetails = AccommodationAddressDetails(
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

  fun toVerificationStatus(status: EntityVerificationStatus): VerificationStatus = VerificationStatus.valueOf(status.name)

  fun toNextAccommodationStatus(status: EntityNextAccommodationStatus): NextAccommodationStatus = NextAccommodationStatus.valueOf(status.name)
}
