package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import java.time.OffsetDateTime
import java.util.UUID

data class Cas2Application(
  val uiUrl: String,
  val id: UUID,
  val submittedApplication: Cas2SubmittedApplicationSummary?,
)

data class Cas2SubmittedApplicationSummary(
  val latestAssessmentStatus: String?,
  val submittedAt: OffsetDateTime,
)
