package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import com.fasterxml.jackson.annotation.JsonCreator
import java.util.UUID

data class Cas3Application(
  val id: UUID,
  val applicationStatus: Cas3ApplicationStatus,
  val assessmentStatus: Cas3AssessmentStatus?,
  val bookingStatus: Cas3BookingStatus?,
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
  CONFIRMED("rejected"),
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
