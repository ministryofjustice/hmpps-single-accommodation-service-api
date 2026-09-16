package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability

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
class Cas2SuitabilityContextUpdater(
  private val sentryService: SentryService,
) : ContextUpdater() {

  override val description = set("Not started and start CAS2 application")

  val notStarted = "notStarted"
  val notSubmitted = "notSubmitted"
  val offerDeclined = "offerDeclined"
  val cancelled = "cancelled"
  val withdrawn = "withdrawn"
  val unknown = "unknown"

  override val outcomes = mapOf(
    notStarted to ServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS2_NOT_STARTED,
      action = CaseAction(type = CaseActionType.START_CAS2_REFERRAL, service = AccommodationService.CAS2),
      link = EligibilityKeys.START_APPLICATION,
      linkType = LinkType.CAS2_START_APPLICATION,
    ),
    notSubmitted to ServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS2_NOT_SUBMITTED,
      action = CaseAction(type = CaseActionType.CONTINUE_A_CAS2_REFERRAL, service = AccommodationService.CAS2),
      link = EligibilityKeys.CONTINUE_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    ),
    offerDeclined to ServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS2_OFFER_DECLINED_OR_WITHDRAWN,
      link = EligibilityKeys.START_NEW_APPLICATION,
      linkType = LinkType.CAS2_START_APPLICATION,
    ),
    cancelled to ServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS2_CANCELLED,
      link = EligibilityKeys.START_NEW_APPLICATION,
      linkType = LinkType.CAS2_START_APPLICATION,
    ),
    withdrawn to ServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS2_WITHDRAWN,
      link = EligibilityKeys.START_NEW_APPLICATION,
      linkType = LinkType.CAS2_START_APPLICATION,
    ),
    unknown to ServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS2_UNKNOWN,
    ),
  )

  override fun toServiceResult(context: EvaluationContext) = when {
    context.data.cas2Application == null -> outcome(notStarted)
    context.data.cas2Application.submittedApplication?.submittedAt == null -> outcome(notSubmitted)
    context.data.cas2Application.submittedApplication?.latestAssessmentStatus == "offerDeclined" -> outcome(offerDeclined)
    context.data.cas2Application.submittedApplication?.latestAssessmentStatus == "cancelled" -> outcome(cancelled)
    context.data.cas2Application.submittedApplication?.latestAssessmentStatus == "withdrawn" -> outcome(withdrawn)
    else -> outcome(unknown).also {
      val latestAssessmentStatus = context.data.cas2Application.submittedApplication?.latestAssessmentStatus
      sentryService.captureErrorMessage(
        "CAS2 Service Status unknown because unexpected latest assessment status for ${context.data.crn}: $latestAssessmentStatus",
      )
    }
  }
}
