package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2SubmittedApplicationSummaryDto
import java.time.OffsetDateTime
import java.util.UUID

fun buildCas2ApplicationDto(
  submittedApplication: Cas2SubmittedApplicationSummaryDto? = null,
  id: UUID = UUID.randomUUID(),
  uiUrl: String = "https://cas2-ui/applications/$id",
) = Cas2ApplicationDto(
  uiUrl = uiUrl,
  id = id,
  submittedApplication = submittedApplication,
)

fun buildCas2ApplicationSummaryDto(
  latestAssessmentStatus: String? = null,
  submittedAt: OffsetDateTime = OffsetDateTime.now(),
) = Cas2SubmittedApplicationSummaryDto(
  submittedAt = submittedAt,
  latestAssessmentStatus = latestAssessmentStatus,
)
