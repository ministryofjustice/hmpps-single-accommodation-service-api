package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas3SuitabilityContextUpdater : ContextUpdater() {

  override val description = set("status from referral")

  val startNewReferral = "startNewReferral"
  val rejected = "rejected"
  val notSubmitted = "notSubmitted"
  val startReferral = "startReferral"

  override val outcomes = mapOf(
    startNewReferral to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS3_NOT_STARTED,
      link = EligibilityKeys.START_NEW_REFERRAL,
      linkType = LinkType.CAS3_START_REFERRAL,
    ),
    rejected to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS3_REJECTED,
      link = EligibilityKeys.START_NEW_REFERRAL,
      linkType = LinkType.CAS3_START_REFERRAL,
    ),
    notSubmitted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS3_NOT_SUBMITTED,
      link = EligibilityKeys.CONTINUE_REFERRAL,
      linkType = LinkType.CAS3_VIEW_REFERRAL,
    ),
    startReferral to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS3_NOT_STARTED,
      link = EligibilityKeys.START_REFERRAL,
      linkType = LinkType.CAS3_START_REFERRAL,
    ),
  )

  override fun toServiceResult(context: EvaluationContext): ServiceResultNew {
    val applicationStatus = context.data.cas3Application?.applicationStatus
    val assessmentStatus = context.data.cas3Application?.submittedApplication?.assessmentStatus
    val bookingStatus = context.data.cas3Application?.bookingStatus
    return when (bookingStatus) {
      Cas3BookingStatus.ARRIVED,
      Cas3BookingStatus.CLOSED,
      Cas3BookingStatus.DEPARTED,
      -> outcome(startNewReferral)

      else -> when (assessmentStatus) {
        Cas3AssessmentStatus.CLOSED -> outcome(startNewReferral)
        Cas3AssessmentStatus.REJECTED -> outcome(rejected)
        else -> when (applicationStatus) {
          Cas3ApplicationStatus.IN_PROGRESS -> outcome(notSubmitted)
          Cas3ApplicationStatus.REJECTED -> outcome(rejected)
          else -> outcome(startReferral)
        }
      }
    }
  }
}
