package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import com.fasterxml.jackson.annotation.JsonCreator
import java.time.LocalDate
import java.util.UUID

data class Cas3Application(
  val id: UUID,
  val applicationStatus: Cas3ApplicationStatus,
  val applicationSubmittedDate: LocalDate?,
  val applicationSubmittedBy: Cas3Staff,
  val applicationRejectedReason: String?,
  val assessmentStatus: Cas3AssessmentStatus?,
  val bookingStatus: Cas3BookingStatus?,
  val bookingProvisionalOfferSentDate: LocalDate?,
  val previousBookings: List<Cas3ExternalPreviousBooking>?,
  val premises: Cas3PremisesSummary?,
  val uiUrl: String,
)

data class Cas3ExternalPreviousBooking(
  val bookingStatus: Cas3BookingStatus?,
  val cancellation: Cas3ExternalPreviousBookingCancellation?,
)

data class Cas3ExternalPreviousBookingCancellation(
  val cancellationDate: LocalDate?,
  val cancellationReason: String?,
)

data class Cas3Staff(
  val name: String,
  val username: String,
  val staffCode: String,
)

data class Cas3PremisesSummary(
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
