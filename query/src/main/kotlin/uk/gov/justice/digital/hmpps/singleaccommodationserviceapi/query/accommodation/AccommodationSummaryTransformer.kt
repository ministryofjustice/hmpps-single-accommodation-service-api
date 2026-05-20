package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddress
import java.time.LocalDate

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
}
