package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas2CompletionContextUpdater(
  private val sentryService: SentryService,
) : ContextUpdater() {

  override val description = set("Started and continue CAS2 application")

  val submitted = "submitted"
  val moreInfoRequested = "moreInfoRequested"
  val awaitingDecision = "awaitingDecision"
  val onWaitingList = "onWaitingList"
  val placeOffered = "placeOffered"
  val offerAccepted = "offerAccepted"
  val unknown = "unknown"

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
  )

  override fun toServiceResult(context: EvaluationContext) = when (context.data.cas2Application?.submittedApplication?.latestAssessmentStatus) {
    null -> outcome(submitted)
    Cas2AssessmentStatus.MORE_INFO_REQUESTED -> outcome(moreInfoRequested)
    Cas2AssessmentStatus.AWAITING_DECISION -> outcome(awaitingDecision)
    Cas2AssessmentStatus.ON_WAITING_LIST -> outcome(onWaitingList)
    Cas2AssessmentStatus.PLACE_OFFERED -> outcome(placeOffered)
    Cas2AssessmentStatus.OFFER_ACCEPTED -> outcome(offerAccepted)
    else -> outcome(unknown).also {
      val latestAssessmentStatus = context.data.cas2Application.submittedApplication?.latestAssessmentStatus
      sentryService.captureErrorMessage(
        "CAS2 Service Status unknown because unexpected latest assessment status for ${context.data.crn}: $latestAssessmentStatus",
      )
    }
  }
}
