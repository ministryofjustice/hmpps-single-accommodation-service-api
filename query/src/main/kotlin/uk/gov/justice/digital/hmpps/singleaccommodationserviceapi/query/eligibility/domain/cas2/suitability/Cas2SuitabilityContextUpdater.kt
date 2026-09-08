package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability

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
class Cas2SuitabilityContextUpdater(
  private val sentryService: SentryService,
) : ContextUpdater() {

  override fun toServiceResult(context: EvaluationContext) = when {
    context.data.cas2Application == null -> ServiceResult(
      serviceStatus = ServiceStatus.NOT_STARTED,
      action = CaseAction(CaseActionType.START_CAS2_REFERRAL),
      link = EligibilityKeys.START_APPLICATION,
      linkType = LinkType.CAS2_START_APPLICATION,
    )

    context.data.cas2Application.submittedApplication?.submittedAt == null -> ServiceResult(
      serviceStatus = ServiceStatus.NOT_SUBMITTED,
      action = CaseAction(CaseActionType.CONTINUE_A_CAS2_REFERRAL),
      link = EligibilityKeys.CONTINUE_APPLICATION,
      linkType = LinkType.CAS2_VIEW_APPLICATION,
    )

    context.data.cas2Application.submittedApplication?.latestAssessmentStatus == "offerDeclined" -> ServiceResult(
      serviceStatus = ServiceStatus.OFFER_DECLINED_OR_WITHDRAWN,
      link = EligibilityKeys.START_NEW_APPLICATION,
      linkType = LinkType.CAS2_START_APPLICATION,
    )

    context.data.cas2Application.submittedApplication?.latestAssessmentStatus == "cancelled" -> ServiceResult(
      serviceStatus = ServiceStatus.CANCELLED,
      link = EligibilityKeys.START_NEW_APPLICATION,
      linkType = LinkType.CAS2_START_APPLICATION,
    )

    context.data.cas2Application.submittedApplication?.latestAssessmentStatus == "withdrawn" -> ServiceResult(
      serviceStatus = ServiceStatus.WITHDRAWN,
      link = EligibilityKeys.START_NEW_APPLICATION,
      linkType = LinkType.CAS2_START_APPLICATION,
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
