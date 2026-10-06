package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas2SuitabilityContextUpdater(
  private val sentryService: SentryService,
) : ContextUpdater() {

  override val description = set("Not started and start CAS2 application")

  val notStartedCommunity = "notStartedCommunity"
  val notStartedPrison = "notStartedPrison"
  val notStartedCas1 = "notStartedCas1"
  val notStartedCas2 = "notStartedCas2"
  val notSubmitted = "notSubmitted"
  val offerDeclined = "offerDeclined"
  val cancelled = "cancelled"
  val withdrawn = "withdrawn"
  val unknown = "unknown"

  override val outcomes = mapOf(
    notStartedCommunity to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_NOT_STARTED_COMMUNITY,
      link = EligibilityKeys.START_REFERRAL,
      linkType = LinkType.CAS2_START_APPLICATION,
    ),
    notStartedPrison to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_NOT_STARTED_PRISON,
      link = EligibilityKeys.START_APPLICATION,
      linkType = LinkType.CAS2_START_APPLICATION,
    ),
    notStartedCas1 to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_NOT_STARTED_CAS1,
      link = EligibilityKeys.START_APPLICATION,
      linkType = LinkType.CAS2_START_APPLICATION,
    ),
    notStartedCas2 to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_NOT_STARTED_CAS2,
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

  fun toNotStartedServiceResult(context: EvaluationContext) = when {
    context.data.currentAccommodationTypeEntity?.isCas1 == true -> outcome(notStartedCas1)
    context.data.currentAccommodationTypeEntity?.isCas2 == true -> outcome(notStartedCas2)
    context.data.currentAccommodationTypeEntity?.isPrison == true -> outcome(notStartedPrison)
    else -> outcome(notStartedCommunity)
  }

  override fun toServiceResult(context: EvaluationContext) = when {
    context.data.cas2Application == null -> toNotStartedServiceResult(context)
    context.data.cas2Application.submittedApplication?.submittedAt == null -> outcome(notSubmitted)
    context.data.cas2Application.submittedApplication?.latestAssessmentStatus == Cas2AssessmentStatus.OFFER_DECLINED -> outcome(offerDeclined)
    context.data.cas2Application.submittedApplication?.latestAssessmentStatus == Cas2AssessmentStatus.CANCELLED -> outcome(cancelled)
    context.data.cas2Application.submittedApplication?.latestAssessmentStatus == Cas2AssessmentStatus.WITHDRAWN -> outcome(withdrawn)
    else -> outcome(unknown).also {
      val latestAssessmentStatus = context.data.cas2Application.submittedApplication?.latestAssessmentStatus
      sentryService.captureErrorMessage(
        "CAS2 Service Status unknown because unexpected latest assessment status for ${context.data.crn}: $latestAssessmentStatus",
      )
    }
  }
}
