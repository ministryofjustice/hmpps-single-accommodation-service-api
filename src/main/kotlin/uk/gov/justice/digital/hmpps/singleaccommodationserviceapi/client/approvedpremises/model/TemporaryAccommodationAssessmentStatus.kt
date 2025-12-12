package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.model

@Suppress("EnumEntryName")
enum class TemporaryAccommodationAssessmentStatus(val value: String) {

  unallocated("unallocated"),
  inReview("in_review"),
  readyToPlace("ready_to_place"),
  closed("closed"),
  rejected("rejected"),
}
