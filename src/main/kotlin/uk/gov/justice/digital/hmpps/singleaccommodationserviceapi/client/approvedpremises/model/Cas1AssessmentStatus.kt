package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model

@Suppress("EnumEntryName")
enum class Cas1AssessmentStatus(val value: String) {
  awaitingResponse("awaiting_response"),
  completed("completed"),
  reallocated("reallocated"),
  inProgress("in_progress"),
  notStarted("not_started"),
}
