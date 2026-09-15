package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.OffsetDateTime
import java.util.UUID

data class Cas2ApplicationDto(
  val uiUrl: String,
  val id: UUID,
  val submittedApplication: Cas2SubmittedApplicationSummaryDto?,
)

data class Cas2SubmittedApplicationSummaryDto(
  val latestAssessmentStatus: String?,
  val submittedAt: OffsetDateTime,
)
