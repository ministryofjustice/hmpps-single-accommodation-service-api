package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class Cas1AssessmentStatus(val value: String) {
  AWAITING_RESPONSE("awaiting_response"),
  COMPLETED("completed"),
  REALLOCATED("reallocated"),
  IN_PROGRESS("in_progress"),
  NOT_STARTED("not_started"),
  ;

  @JsonValue
  fun toValue(): String = value

  companion object {
    @JvmStatic
    @JsonCreator
    fun forValue(value: String): Cas1AssessmentStatus = Cas1AssessmentStatus.values()
      .first { it.value == value }
  }
}
