package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.arrived

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas2NotArrivedContextUpdater : ContextUpdater() {
  override val terminalNode = true

  val moreInfoRequested = "moreInfoRequested"
  val awaitingDecision = "awaitingDecision"
  val onWaitingList = "onWaitingList"
  val placeOffered = "placeOffered"
  val offerAccepted = "offerAccepted"
  val unknown = "unknown"
  val awaitingArrival = "awaitingArrival"
  val submitted = "submitted"

  override val outcomes = mapOf(
    moreInfoRequested to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_MORE_INFORMATION_NEEDED,
    ),
    awaitingDecision to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_AWAITING_DECISION,
    ),
    onWaitingList to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_ON_WAITING_LIST,
    ),
    placeOffered to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_PLACE_OFFERED,
    ),
    offerAccepted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_OFFER_ACCEPTED,
    ),
    unknown to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_UNKNOWN,
    ),
    awaitingArrival to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_AWAITING_ARRIVAL,
    ),
    submitted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_SUBMITTED,
    ),
  )

  override fun toServiceResult(context: EvaluationContext) = when (context.data.cas2Application?.submittedApplication?.latestAssessmentStatus) {
    Cas2AssessmentStatus.MORE_INFO_REQUESTED -> outcome(moreInfoRequested)
    Cas2AssessmentStatus.AWAITING_DECISION -> outcome(awaitingDecision)
    Cas2AssessmentStatus.ON_WAITING_LIST -> outcome(onWaitingList)
    Cas2AssessmentStatus.PLACE_OFFERED -> outcome(placeOffered)
    Cas2AssessmentStatus.OFFER_ACCEPTED -> outcome(offerAccepted)
    Cas2AssessmentStatus.AWAITING_ARRIVAL -> outcome(awaitingArrival)
    else -> outcome(submitted)
  }
}
