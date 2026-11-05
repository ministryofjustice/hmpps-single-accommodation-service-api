package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext

@Component
class NoPlacementContextUpdater : ContextUpdater() {

  val placementRequestWithdrawn = "placementRequestWithdrawn"
  val placementRequestUnsubmitted = "placementRequestUnsubmitted"
  val placementRequestRejected = "placementRequestRejected"
  val placementRequestSubmitted = "placementRequestSubmitted"

  override val outcomes = mapOf(
    placementRequestWithdrawn to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_REQUEST_WITHDRAWN,
    ),
    placementRequestUnsubmitted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_REQUEST_NOT_SUBMITTED,
    ),
    placementRequestRejected to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_REQUEST_REJECTED,
    ),
    placementRequestSubmitted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_REQUEST_SUBMITTED,
    ),
  )

  override val terminalNode = true

  override fun toServiceResult(context: EvaluationContext): ServiceResultNew {
    val requestForPlacementStatus = context.data.cas1Application?.currentOutcome?.requestForPlacement?.status

    return when (requestForPlacementStatus) {
      Cas1RequestForPlacementStatus.REQUEST_WITHDRAWN -> outcome(placementRequestWithdrawn)
      Cas1RequestForPlacementStatus.REQUEST_UNSUBMITTED -> outcome(placementRequestUnsubmitted)
      Cas1RequestForPlacementStatus.REQUEST_REJECTED -> outcome(placementRequestRejected)
      else -> outcome(placementRequestSubmitted)
    }
  }
}
