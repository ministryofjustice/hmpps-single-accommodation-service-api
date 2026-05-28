package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationStatusEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import java.time.LocalDate
import java.time.ZoneId

object AccommodationSummaryTransformer {
  fun toAccommodationSummary(
    crn: String,
    address: CanonicalAddress,
  ) = AccommodationSummaryDto(
    crn = crn,
    startDate = address.startDate?.let { LocalDate.parse(it) },
    endDate = address.endDate?.let { LocalDate.parse(it) },
    address = AccommodationAddressDetails(
      postcode = address.postcode,
      subBuildingName = address.subBuildingName,
      buildingName = address.buildingName,
      buildingNumber = address.buildingNumber,
      thoroughfareName = address.thoroughfareName,
      dependentLocality = address.dependentLocality,
      postTown = address.postTown,
      county = address.county,
      country = address.countryCode,
      uprn = address.uprn,
    ),
    status = address.status.code?.let {
      AccommodationStatusDto(
        code = it,
        description = address.status.description,
      )
    },
    type = address.usages
      .firstOrNull { it.isActive && it.usageCode.code != null }
      ?.let {
        AccommodationTypeDto(
          code = it.usageCode.code!!,
          description = it.usageCode.description,
        )
      },
  )

  fun toAccommodationSummary(
    crn: String,
    proposedAccommodationEntity: ProposedAccommodationEntity,
    accommodationTypeEntity: AccommodationTypeEntity,
    accommodationStatusEntity: AccommodationStatusEntity?,
  ) = AccommodationSummaryDto(
    crn = crn,
    cprAddressId = proposedAccommodationEntity.cprAddressId,
    startDate = proposedAccommodationEntity.createdAt?.atZone(ZoneId.systemDefault())?.toLocalDate(),
    endDate = null,
    address = AccommodationAddressDetails(
      postcode = proposedAccommodationEntity.postcode,
      subBuildingName = proposedAccommodationEntity.subBuildingName,
      buildingName = proposedAccommodationEntity.buildingName,
      buildingNumber = proposedAccommodationEntity.buildingNumber,
      thoroughfareName = proposedAccommodationEntity.throughfareName,
      dependentLocality = proposedAccommodationEntity.dependentLocality,
      postTown = proposedAccommodationEntity.postTown,
      county = proposedAccommodationEntity.county,
      country = proposedAccommodationEntity.country,
      uprn = proposedAccommodationEntity.uprn,
    ),
    status = accommodationStatusEntity?.let {
      AccommodationStatusDto(
        code = it.code,
        description = it.name,
      )
    },
    type = accommodationTypeEntity?.let {
      AccommodationTypeDto(
        code = it.code,
        description = it.name,
      )
    },
  )
}
