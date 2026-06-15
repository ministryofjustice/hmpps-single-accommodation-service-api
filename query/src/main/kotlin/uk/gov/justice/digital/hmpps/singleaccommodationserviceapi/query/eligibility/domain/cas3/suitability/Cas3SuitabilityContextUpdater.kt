package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability

import org.springframework.stereotype.Component
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

  override fun toServiceResult(context: EvaluationContext): ServiceResult {
    val applicationStatus = context.data.cas3Application?.applicationStatus
    val assessmentStatus = context.data.cas3Application?.assessmentStatus
    val bookingStatus = context.data.cas3Application?.bookingStatus
    return when (bookingStatus) {
      Cas3BookingStatus.ARRIVED,
      Cas3BookingStatus.CLOSED,
      Cas3BookingStatus.DEPARTED,
      -> ServiceResult(
        serviceStatus = ServiceStatus.NOT_STARTED,
        action = EligibilityKeys.START_CAS3_REFERRAL,
        link = EligibilityKeys.START_NEW_REFERRAL,
        linkType = LinkType.CAS3_START_REFERRAL,
      )

      else -> when (assessmentStatus) {
        Cas3AssessmentStatus.CLOSED -> ServiceResult(
          serviceStatus = ServiceStatus.NOT_STARTED,
          action = EligibilityKeys.START_CAS3_REFERRAL,
          link = EligibilityKeys.START_NEW_REFERRAL,
          linkType = LinkType.CAS3_START_REFERRAL,
        )

        Cas3AssessmentStatus.REJECTED -> ServiceResult(
          serviceStatus = ServiceStatus.REJECTED,
          action = EligibilityKeys.START_CAS3_REFERRAL,
          link = EligibilityKeys.START_NEW_REFERRAL,
          linkType = LinkType.CAS3_START_REFERRAL,
        )

        else -> when (applicationStatus) {
          Cas3ApplicationStatus.IN_PROGRESS -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_SUBMITTED,
            link = EligibilityKeys.VIEW_REFERRAL,
            linkType = LinkType.CAS3_VIEW_REFERRAL,
          )

          Cas3ApplicationStatus.REJECTED -> ServiceResult(
            serviceStatus = ServiceStatus.REJECTED,
            action = EligibilityKeys.START_CAS3_REFERRAL,
            link = EligibilityKeys.START_NEW_REFERRAL,
            linkType = LinkType.CAS3_START_REFERRAL,
          )

          else -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_STARTED,
            action = EligibilityKeys.START_CAS3_REFERRAL,
            link = EligibilityKeys.START_REFERRAL,
            linkType = LinkType.CAS3_START_REFERRAL,
          )
        }
      }
    }
  }
}
