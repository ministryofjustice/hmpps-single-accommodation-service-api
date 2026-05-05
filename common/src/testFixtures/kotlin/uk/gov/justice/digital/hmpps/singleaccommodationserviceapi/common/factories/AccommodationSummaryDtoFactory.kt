package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import java.time.LocalDate

fun buildAccommodationSummaryDto(
  crn: String = "X12345",
  address: AccommodationAddressDetails = buildAccommodationAddressDetails(),
  startDate: LocalDate? = null,
  endDate: LocalDate? = null,
  status: AccommodationStatusDto? = buildAccommodationStatusDto(),
  type: AccommodationTypeDto? = buildAccommodationTypeDto(),
) = AccommodationSummaryDto(
  crn = crn,
  address = address,
  startDate = startDate,
  endDate = endDate,
  status = status,
  type = type,
)

fun buildAccommodationTypeDto(
  code: AccommodationTypeCode = AccommodationTypeCode.A02,
  description: String = "Approved Premises",
) = AccommodationTypeDto(
  code = code,
  description = description,
)

fun buildAccommodationStatusDto(
  code: AccommodationStatusCode = AccommodationStatusCode.M,
  description: String = "Main",
) = AccommodationStatusDto(
  code = code,
  description = description,
)
