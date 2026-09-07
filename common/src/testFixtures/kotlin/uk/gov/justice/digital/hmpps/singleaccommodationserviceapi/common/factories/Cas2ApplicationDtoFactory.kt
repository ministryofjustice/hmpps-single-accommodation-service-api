package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas2ApplicationSummaryDto
import java.util.UUID

fun buildCas2ApplicationDto(
  application: Cas2ApplicationSummaryDto = buildCas2ApplicationSummaryDto(),
  uiUrl: String = "https://cas2-ui/applications/${application.id}",
) = Cas2ApplicationDto(
  uiUrl = uiUrl,
  application = application,
)

fun buildCas2ApplicationSummaryDto(
  id: UUID = UUID.randomUUID(),
  status: String = "STARTED",
) = Cas2ApplicationSummaryDto(
  id = id,
  status = status,
)
