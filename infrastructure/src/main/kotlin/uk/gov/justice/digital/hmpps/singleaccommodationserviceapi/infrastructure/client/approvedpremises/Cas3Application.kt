package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import com.fasterxml.jackson.annotation.JsonCreator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.util.requireXor
import java.time.LocalDate
import java.util.UUID

data class Cas3Application(
  val id: UUID,
  val applicationStatus: Cas3ApplicationStatus,
  val submittedApplication: Cas3SubmittedApplicationDto?,
  @Deprecated("Use submittedApplication instead")
  val assessmentStatus: Cas3AssessmentStatus?,
  @Deprecated("Use submittedApplication instead")
  val bookingStatus: Cas3BookingStatus?,
  @Deprecated("Use submittedApplication instead")
  val bookingProvisionalOfferSentDate: LocalDate?,
  @Deprecated("Use submittedApplication instead")
  val previousBookings: List<Cas3PreviousBookingDto>?,
  @Deprecated("Use submittedApplication instead")
  val premises: Cas3LatestBookingPremisesDto?,
  val uiUrl: String,
) {
  init {
    requireXor(
      applicationStatus == Cas3ApplicationStatus.IN_PROGRESS,
      submittedApplication != null,
    ) {
      "A submitted application is required for any status other than 'inProgress'"
    }
  }
}

data class Cas3SubmittedApplicationDto(
  val submittedDate: LocalDate,
  val submittedBy: Cas3Staff,
  val assessmentStatus: Cas3AssessmentStatus?,
  val assessmentRejectionReason: String?,
  val latestBooking: Cas3LatestBookingDto?,
) {
  init {
    requireXor(
      assessmentStatus == Cas3AssessmentStatus.REJECTED,
      assessmentRejectionReason == null,
    ) {
      "Assessment rejection reason can only be provided if status is `rejected`"
    }
  }
}

data class Cas3LatestBookingDto(
  val status: Cas3BookingStatus?,
  val provisionalOfferSentDate: LocalDate?,
  val premises: Cas3LatestBookingPremisesDto,
)

data class Cas3PreviousBookingDto(
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

data class Cas3LatestBookingPremisesDto(
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
