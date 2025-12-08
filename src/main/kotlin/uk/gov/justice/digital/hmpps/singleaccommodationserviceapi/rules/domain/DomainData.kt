package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import java.time.OffsetDateTime
import java.util.UUID

data class DomainData(
  val tier: String,
  val sex: Sex,
  val releaseDate: OffsetDateTime,
  val cas1Application: Cas1Application? = null,
)

data class Cas1Application(
  val id: UUID,
  val status: ApprovedPremisesApplicationStatus = ApprovedPremisesApplicationStatus.Started,
)

enum class ApprovedPremisesApplicationStatus(val value: String) {
  Started("started"), // in draft - forget
  Rejected("rejected"), // forget
  AwaitingAssesment("awaitingAssesment"), // SUBMITTED
  UnallocatedAssesment("unallocatedAssesment"), // SUBMITTED
  AssesmentInProgress("assesmentInProgress"), // SUBMITTED
  AwaitingPlacement("awaitingPlacement"), // SUBMITTED
  PlacementAllocated("placementAllocated"), // CONFIRMED
  Inapplicable("inapplicable"), // forget
  Withdrawn("withdrawn"), // forget
  RequestedFurtherInformation("requestedFurtherInformation"), // SUBMITTED
  PendingPlacementRequest("pendingPlacementRequest"), // SUBMITTED
  Expired("expired"), // forget
}
