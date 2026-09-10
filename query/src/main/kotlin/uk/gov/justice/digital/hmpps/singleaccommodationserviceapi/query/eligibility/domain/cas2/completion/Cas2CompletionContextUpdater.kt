package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.completion

import org.springframework.stereotype.Component
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

  override fun toServiceResult(context: EvaluationContext) = when (context.data.cas2Application?.submittedApplication?.latestAssessmentStatus) {
    null -> ServiceResult(
      serviceStatus = ServiceStatus.SUBMITTED,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    )

    "moreInfoRequested" -> ServiceResult(
      serviceStatus = ServiceStatus.MORE_INFORMATION_NEEDED,
      action = CaseAction(CaseActionType.PROVIDE_MORE_INFORMATION_FOR_CAS2_REFERRAL),
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    )

    "awaitingDecision" -> ServiceResult(
      serviceStatus = ServiceStatus.AWAITING_DECISION,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    )

    "onWaitingList" -> ServiceResult(
      serviceStatus = ServiceStatus.ON_WAITING_LIST,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    )

    "placeOffered" -> ServiceResult(
      serviceStatus = ServiceStatus.PLACE_OFFERED,
      action = CaseAction(CaseActionType.REPLY_TO_CAS2_PLACE_OFFER),
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    )

    "offerAccepted" -> ServiceResult(
      serviceStatus = ServiceStatus.OFFER_ACCEPTED,
      link = EligibilityKeys.VIEW_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    )

    else -> ServiceResult(
      serviceStatus = ServiceStatus.UNKNOWN,
    ).also {
      val latestAssessmentStatus = context.data.cas2Application.submittedApplication?.latestAssessmentStatus
      sentryService.captureErrorMessage(
        "CAS2 Service Status unknown because unexpected latest assessment status for ${context.data.crn}: $latestAssessmentStatus",
      )
    }
  }
}
