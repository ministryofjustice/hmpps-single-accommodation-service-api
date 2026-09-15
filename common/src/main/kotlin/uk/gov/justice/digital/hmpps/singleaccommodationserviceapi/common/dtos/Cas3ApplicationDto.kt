package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import com.fasterxml.jackson.annotation.JsonCreator
import java.time.LocalDate
import java.util.UUID

data class Cas3ApplicationDto(
  val id: UUID,
  val applicationStatus: Cas3ApplicationStatus,
  val applicationSubmittedDate: LocalDate?,
  val applicationSubmittedBy: Cas3StaffDto,
  val applicationRejectedReason: String?,
  val assessmentStatus: Cas3AssessmentStatus?,
  val bookingStatus: Cas3BookingStatus?,
  val bookingProvisionalOfferSentDate: LocalDate?,
  val previousBookings: List<Cas3ExternalPreviousBookingDto>?,
  val premises: Cas3PremisesSummaryDto?,
  val uiUrl: String,
)

data class Cas3ExternalPreviousBookingDto(
  val bookingStatus: Cas3BookingStatus?,
  val cancellation: Cas3ExternalPreviousBookingCancellationDto?,
)

data class Cas3ExternalPreviousBookingCancellationDto(
  val cancellationDate: LocalDate?,
  val cancellationReason: String?,
)

data class Cas3StaffDto(
  val name: String,
  val username: String,
  val staffCode: String,
)

data class Cas3PremisesSummaryDto(
  val name: String?,
  val startDate: LocalDate?,
  val endDate: LocalDate?,
  val addressLine1: String,
  val addressLine2: String?,
  val town: String?,
  val postcode: String,
)

enum class Cas3ApplicationStatus(val casValue: String) {
  IN_PROGRESS("inProgress"),
  SUBMITTED("submitted"),
  REQUESTED_FURTHER_INFORMATION("requestedFurtherInformation"),
  REJECTED("rejected"),
  ;

  companion object {
    @JsonCreator
    @JvmStatic
    fun from(value: String): Cas3ApplicationStatus = Cas3ApplicationStatus.entries.first { it.casValue == value || it.name == value }
  }
}

enum class Cas3AssessmentStatus(val casValue: String) {
  UNALLOCATED("unallocated"),
  IN_REVIEW("in_review"),
  READY_TO_PLACE("ready_to_place"),
  CLOSED("closed"),
  REJECTED("rejected"),
  ;

  companion object {
    @JsonCreator
    @JvmStatic
    fun from(value: String): Cas3AssessmentStatus = Cas3AssessmentStatus.entries.first { it.casValue == value || it.name == value }
  }
}

enum class Cas3BookingStatus(val casValue: String) {
  PROVISIONAL("provisional"),
  CONFIRMED("confirmed"),
  ARRIVED("arrived"),
  NOT_MINUS_ARRIVED("notMinusArrived"),
  DEPARTED("departed"),
  CANCELLED("cancelled"),
  CLOSED("closed"),
  ;

  companion object {
    @JsonCreator
    @JvmStatic
    fun from(value: String): Cas3BookingStatus = Cas3BookingStatus.entries.first { it.casValue == value || it.name == value }
  }
}
