package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability

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
    notStarted to ServiceResult(
      serviceStatus = ServiceStatus.NOT_STARTED,
      action = CaseAction(type = CaseActionType.START_CAS2_REFERRAL, service = AccommodationService.CAS2),
      link = EligibilityKeys.START_APPLICATION,
      linkType = LinkType.CAS2_START_APPLICATION,
    ),
    notSubmitted to ServiceResult(
      serviceStatus = ServiceStatus.NOT_SUBMITTED,
      action = CaseAction(type = CaseActionType.CONTINUE_A_CAS2_REFERRAL, service = AccommodationService.CAS2),
      link = EligibilityKeys.CONTINUE_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    ),
    offerDeclined to ServiceResult(
      serviceStatus = ServiceStatus.OFFER_DECLINED_OR_WITHDRAWN,
      link = EligibilityKeys.START_NEW_APPLICATION,
      linkType = LinkType.CAS2_START_APPLICATION,
    ),
    cancelled to ServiceResult(
      serviceStatus = ServiceStatus.CANCELLED,
      link = EligibilityKeys.START_NEW_APPLICATION,
      linkType = LinkType.CAS2_START_APPLICATION,
    ),
    withdrawn to ServiceResult(
      serviceStatus = ServiceStatus.WITHDRAWN,
      link = EligibilityKeys.START_NEW_APPLICATION,
      linkType = LinkType.CAS2_START_APPLICATION,
    ),
    unknown to ServiceResult(
      serviceStatus = ServiceStatus.UNKNOWN,
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
