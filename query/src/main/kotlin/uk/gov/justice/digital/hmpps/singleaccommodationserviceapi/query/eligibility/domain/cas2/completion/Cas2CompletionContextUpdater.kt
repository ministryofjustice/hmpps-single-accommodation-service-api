package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
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
    submitted to ServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS2_SUBMITTED,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    ),
    moreInfoRequested to ServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS2_MORE_INFORMATION_NEEDED,
      action = CaseAction(type = CaseActionType.PROVIDE_MORE_INFORMATION_FOR_CAS2_REFERRAL, service = AccommodationService.CAS2),
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    ),
    awaitingDecision to ServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS2_AWAITING_DECISION,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    ),
    onWaitingList to ServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS2_ON_WAITING_LIST,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    ),
    placeOffered to ServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS2_PLACE_OFFERED,
      action = CaseAction(type = CaseActionType.REPLY_TO_CAS2_PLACE_OFFER, service = AccommodationService.CAS2),
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    ),
    offerAccepted to ServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS2_OFFER_ACCEPTED,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    ),
    unknown to ServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS2_UNKNOWN,
    ),
  )

  override fun toServiceResult(context: EvaluationContext) = when (context.data.cas2Application?.submittedApplication?.latestAssessmentStatus) {
    null -> outcome(submitted)
    "moreInfoRequested" -> outcome(moreInfoRequested)
    "awaitingDecision" -> outcome(awaitingDecision)
    "onWaitingList" -> outcome(onWaitingList)
    "placeOffered" -> outcome(placeOffered)
    "offerAccepted" -> outcome(offerAccepted)
    else -> outcome(unknown).also {
      val latestAssessmentStatus = context.data.cas2Application.submittedApplication?.latestAssessmentStatus
      sentryService.captureErrorMessage(
        "CAS2 Service Status unknown because unexpected latest assessment status for ${context.data.crn}: $latestAssessmentStatus",
      )
    }
  }
}
