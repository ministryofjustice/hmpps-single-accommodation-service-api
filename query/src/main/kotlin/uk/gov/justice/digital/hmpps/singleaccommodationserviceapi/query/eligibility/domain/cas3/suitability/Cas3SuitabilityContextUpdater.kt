package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
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
    startNewReferral to ServiceResult(
      serviceStatus = ServiceStatus.NOT_STARTED,
      action = CaseAction(type = CaseActionType.START_CAS3_REFERRAL, service = AccommodationService.CAS3),
      link = EligibilityKeys.START_NEW_REFERRAL,
      linkType = LinkType.CAS3_START_REFERRAL,
    ),
    rejected to ServiceResult(
      serviceStatus = ServiceStatus.REJECTED,
      action = CaseAction(type = CaseActionType.START_CAS3_REFERRAL, service = AccommodationService.CAS3),
      link = EligibilityKeys.START_NEW_REFERRAL,
      linkType = LinkType.CAS3_START_REFERRAL,
    ),
    notSubmitted to ServiceResult(
      serviceStatus = ServiceStatus.NOT_SUBMITTED,
    ),
    startReferral to ServiceResult(
      serviceStatus = ServiceStatus.NOT_STARTED,
      action = CaseAction(type = CaseActionType.START_CAS3_REFERRAL, service = AccommodationService.CAS3),
      link = EligibilityKeys.START_REFERRAL,
      linkType = LinkType.CAS3_START_REFERRAL,
    ),
  )

  override fun toServiceResult(context: EvaluationContext): ServiceResult {
    val applicationStatus = context.data.cas3Application?.applicationStatus
    val assessmentStatus = context.data.cas3Application?.assessmentStatus
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
