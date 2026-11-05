package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext

@Component
class DepartedPlacementContextUpdater : ContextUpdater() {

  val placementRequestNotStarted = "placementRequestNotStarted"

  override val outcomes = mapOf(
    placementRequestNotStarted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_REQUEST_NOT_STARTED,
    ),
  )

  override val terminalNode = true

  override fun toServiceResult(context: EvaluationContext) = outcome(placementRequestNotStarted)
}
