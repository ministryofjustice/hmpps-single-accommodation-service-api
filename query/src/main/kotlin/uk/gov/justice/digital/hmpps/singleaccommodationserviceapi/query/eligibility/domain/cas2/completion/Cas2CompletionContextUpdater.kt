package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
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
    submitted to ServiceResult(
      serviceStatus = ServiceStatus.SUBMITTED,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    ),
    moreInfoRequested to ServiceResult(
      serviceStatus = ServiceStatus.MORE_INFORMATION_NEEDED,
      action = CaseAction(type = CaseActionType.PROVIDE_MORE_INFORMATION_FOR_CAS2_REFERRAL, service = AccommodationService.CAS2),
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    ),
    awaitingDecision to ServiceResult(
      serviceStatus = ServiceStatus.AWAITING_DECISION,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    ),
    onWaitingList to ServiceResult(
      serviceStatus = ServiceStatus.ON_WAITING_LIST,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    ),
    placeOffered to ServiceResult(
      serviceStatus = ServiceStatus.PLACE_OFFERED,
      action = CaseAction(type = CaseActionType.REPLY_TO_CAS2_PLACE_OFFER, service = AccommodationService.CAS2),
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    ),
    offerAccepted to ServiceResult(
      serviceStatus = ServiceStatus.OFFER_ACCEPTED,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    ),
    unknown to ServiceResult(
      serviceStatus = ServiceStatus.UNKNOWN,
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
