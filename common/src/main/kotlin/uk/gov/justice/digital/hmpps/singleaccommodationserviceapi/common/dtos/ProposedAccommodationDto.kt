package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class ProposedAccommodationDto(
  val id: UUID,
  val crn: String,
  val name: String?,
  val accommodationType: AccommodationTypeDto?,
  val verificationStatus: VerificationStatus?,
  val nextAccommodationStatus: NextAccommodationStatus?,
  val address: AccommodationAddressDetails,
  val startDate: LocalDate?,
  val endDate: LocalDate?,
  val createdBy: String,
  @field:JsonFormat(
    shape = JsonFormat.Shape.STRING,
    pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
    timezone = "UTC",
  )
  val createdAt: Instant,
)

data class ProposedAccommodationDetailCommand(
  val name: String?,
  val accommodationTypeCode: String?,
  val verificationStatus: VerificationStatus,
  val nextAccommodationStatus: NextAccommodationStatus,
  val address: AccommodationAddressDetails,
  val startDate: LocalDate?,
  val endDate: LocalDate?,
)

data class ProposedAccommodationArrivalCommand(
  val arrivalDate: LocalDate,
)

enum class VerificationStatus(override val title: String) : TitleEnum {
  NOT_CHECKED_YET("Not checked"),
  FAILED("Failed"),
  PASSED("Passed"),
}

enum class NextAccommodationStatus(override val title: String) : TitleEnum {
  YES("Yes"),
  NO("No"),
  TO_BE_DECIDED("Not yet"),
}
