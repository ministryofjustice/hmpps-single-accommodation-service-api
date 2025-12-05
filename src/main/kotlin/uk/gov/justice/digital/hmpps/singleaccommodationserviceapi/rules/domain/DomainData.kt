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
  val submittedAt: OffsetDateTime? = null,
  val id: UUID,
  val status: ApprovedPremisesApplicationStatus = ApprovedPremisesApplicationStatus.Started,
)

enum class ApprovedPremisesApplicationStatus(val value: String) {
  Started("started"),
  Rejected("rejected"),
  AwaitingAssesment("awaitingAssesment"),
  UnallocatedAssesment("unallocatedAssesment"),
  AssesmentInProgress("assesmentInProgress"),
  AwaitingPlacement("awaitingPlacement"),
  PlacementAllocated("placementAllocated"),
  Inapplicable("inapplicable"),
  Withdrawn("withdrawn"),
  RequestedFurtherInformation("requestedFurtherInformation"),
  PendingPlacementRequest("pendingPlacementRequest"),
  Expired("expired"),
}
