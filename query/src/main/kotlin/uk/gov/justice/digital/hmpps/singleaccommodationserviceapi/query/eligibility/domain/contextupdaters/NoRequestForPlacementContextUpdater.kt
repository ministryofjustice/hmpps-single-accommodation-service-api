package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext

@Component
class NoRequestForPlacementContextUpdater : ContextUpdater() {

  val placementRequestUnsubmitted = "placementRequestUnsubmitted"
  val infoRequested = "infoRequested"
  val submitted = "submitted"

  override val outcomes = mapOf(
    placementRequestUnsubmitted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_REQUEST_NOT_SUBMITTED,
    ),
    infoRequested to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_INFO_REQUESTED,
    ),
    submitted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_SUBMITTED,
    ),
  )

  override val terminalNode = true

  override fun toServiceResult(context: EvaluationContext): ServiceResultNew {
    val applicationStatus = context.data.cas1Application?.application?.status

    return when (applicationStatus) {
      Cas1ApplicationStatus.REQUESTED_FURTHER_INFORMATION -> outcome(infoRequested)
      Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST -> outcome(placementRequestUnsubmitted)
      else -> outcome(submitted)
    }
  }
}
