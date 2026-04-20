package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.Address

object AccommodationSummaryTransformer {
  fun toAccommodationSummary(
    crn: String,
    address: Address,
  ) = AccommodationSummaryDto(
    crn = crn,
    startDate = address.startDate,
    endDate = address.endDate,
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
    status = address.addressStatus?.let {
      val accommodationStatusCode = AccommodationStatusCode.valueOf(it.name)
      AccommodationStatusDto(
        code = accommodationStatusCode,
        description = accommodationStatusCode.description,
      )
    },
    type = address.addressUsage?.let {
      val accommodationTypeCode = AccommodationTypeCode.valueOf(it.addressUsageCode.name)
      AccommodationTypeDto(
        code = accommodationTypeCode,
        description = accommodationTypeCode.description,
      )
    },
  )
}
