package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class TemporaryAccommodationAssessmentStatus(val value: String) {

  UNALLOCATED("unallocated"),
  IN_REVIEW("in_review"),
  READY_TO_PLACE("ready_to_place"),
  CLOSED("closed"),
  REJECTED("rejected"),
  ;

  @JsonValue
  fun toValue(): String = value

  companion object {
    @JvmStatic
    @JsonCreator
    fun forValue(value: String): TemporaryAccommodationAssessmentStatus = values().first { it.value == value }
  }
}
