package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas2ApplicationData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField

@Component
class Cas2ActiveContextUpdater(
  private val sentryService: SentryService,
) : ContextUpdater<Cas2ApplicationData>() {
  override val inputFields = setOf(DomainDataField.CAS2_ASSESSMENT_STATUS, DomainDataField.CAS2_MARKED_AS_ARRIVED)
  override val terminalNode = true

  val submitted = "submitted"
  val moreInfoRequested = "moreInfoRequested"
  val awaitingDecision = "awaitingDecision"
  val onWaitingList = "onWaitingList"
  val placeOffered = "placeOffered"
  val offerAccepted = "offerAccepted"
  val unknown = "unknown"
  val arrived = "arrived"
  val awaitingArrival = "awaitingArrival"

  override val outcomes = mapOf(
    submitted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_SUBMITTED,
    ),
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
    arrived to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_ARRIVED,
    ),
    awaitingArrival to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_AWAITING_ARRIVAL,
    ),
  )

  override fun toServiceResult(context: EvaluationContext<Cas2ApplicationData>): ServiceResultNew {
    val application = context.data.cas2Application
    return when (application?.submittedApplication?.latestAssessmentStatus) {
      null -> outcome(submitted)
      Cas2AssessmentStatus.MORE_INFO_REQUESTED -> outcome(moreInfoRequested)
      Cas2AssessmentStatus.AWAITING_DECISION -> outcome(awaitingDecision)
      Cas2AssessmentStatus.ON_WAITING_LIST -> outcome(onWaitingList)
      Cas2AssessmentStatus.PLACE_OFFERED -> outcome(placeOffered)
      Cas2AssessmentStatus.OFFER_ACCEPTED -> outcome(offerAccepted)
      Cas2AssessmentStatus.AWAITING_ARRIVAL -> if (application.submittedApplication?.markedAsArrivedDateTime != null) outcome(arrived) else outcome(awaitingArrival)
      else -> outcome(unknown).also {
        val latestAssessmentStatus = application.submittedApplication?.latestAssessmentStatus
        sentryService.captureErrorMessage(
          "CAS2 Service Status unknown because unexpected latest assessment status for ${context.data.crn}: $latestAssessmentStatus",
        )
      }
    }
  }
}
