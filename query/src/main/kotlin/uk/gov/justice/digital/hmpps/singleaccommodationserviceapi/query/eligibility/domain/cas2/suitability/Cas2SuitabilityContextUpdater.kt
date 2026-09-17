package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
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
    notStarted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_NOT_STARTED,
      link = EligibilityKeys.START_APPLICATION,
      linkType = LinkType.CAS2_START_APPLICATION,
    ),
    notSubmitted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_NOT_SUBMITTED,
      link = EligibilityKeys.CONTINUE_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    ),
    offerDeclined to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_OFFER_DECLINED_OR_WITHDRAWN,
      link = EligibilityKeys.START_NEW_APPLICATION,
      linkType = LinkType.CAS2_START_APPLICATION,
    ),
    cancelled to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_CANCELLED,
      link = EligibilityKeys.START_NEW_APPLICATION,
      linkType = LinkType.CAS2_START_APPLICATION,
    ),
    withdrawn to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_WITHDRAWN,
      link = EligibilityKeys.START_NEW_APPLICATION,
      linkType = LinkType.CAS2_START_APPLICATION,
    ),
    unknown to ServiceResultSpec(
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
