package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.buildUpcomingAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isWithinOneYear
import java.time.Clock
import java.time.LocalDate

object Cas3ServiceResultTransformer {
  fun toCas3ServiceResult(data: DomainData, clock: Clock): ServiceResult {
    val today = LocalDate.now(clock)
    return when {
      data.currentAccommodation?.endDate == null -> ServiceResult(ServiceStatus.NOT_ELIGIBLE)
      isWithinOneYear(data.currentAccommodation.endDate, today) -> toCas3ServiceResult(data)
      else -> ServiceResult(
        serviceStatus = ServiceStatus.UPCOMING,
        action = buildUpcomingAction(data.currentAccommodation.endDate, today, EligibilityKeys.START_REFERRAL),
      )
    }
  }

  private fun toCas3ServiceResult(data: DomainData): ServiceResult {
    val applicationStatus = data.cas3Application?.applicationStatus
    val assessmentStatus = data.cas3Application?.assessmentStatus
    val bookingStatus = data.cas3Application?.bookingStatus
    val suitableApplicationId = data.cas3Application?.id
    return when (bookingStatus) {
      Cas3BookingStatus.ARRIVED,
      Cas3BookingStatus.CLOSED,
      Cas3BookingStatus.DEPARTED,
      -> ServiceResult(
        serviceStatus = ServiceStatus.NOT_STARTED,
        suitableApplicationId = suitableApplicationId,
        action = EligibilityKeys.START_CAS3_REFERRAL,
        link = EligibilityKeys.START_NEW_REFERRAL,
      )

      Cas3BookingStatus.PROVISIONAL -> ServiceResult(
        serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
        suitableApplicationId = suitableApplicationId,
        link = EligibilityKeys.VIEW_REFERRAL,
      )

      Cas3BookingStatus.CONFIRMED -> ServiceResult(
        serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
        suitableApplicationId = suitableApplicationId,
        link = EligibilityKeys.VIEW_REFERRAL,
      )

      Cas3BookingStatus.NOT_MINUS_ARRIVED -> ServiceResult(
        serviceStatus = ServiceStatus.NOT_ARRIVED,
        suitableApplicationId = suitableApplicationId,
        link = EligibilityKeys.VIEW_REFERRAL,
      )

      Cas3BookingStatus.CANCELLED -> ServiceResult(
        serviceStatus = ServiceStatus.BOOKING_CANCELLED,
        suitableApplicationId = suitableApplicationId,
        link = EligibilityKeys.VIEW_REFERRAL,
      )

      null -> when (assessmentStatus) {
        Cas3AssessmentStatus.UNALLOCATED,
        Cas3AssessmentStatus.IN_REVIEW,
        Cas3AssessmentStatus.READY_TO_PLACE,
        -> ServiceResult(
          serviceStatus = ServiceStatus.SUBMITTED,
          suitableApplicationId = suitableApplicationId,
          action = EligibilityKeys.WAIT_FOR_CAS3_ASSESSMENT_RESULT,
          link = EligibilityKeys.VIEW_REFERRAL,
        )

        Cas3AssessmentStatus.CLOSED -> ServiceResult(
          serviceStatus = ServiceStatus.NOT_STARTED,
          suitableApplicationId = suitableApplicationId,
          action = EligibilityKeys.START_CAS3_REFERRAL,
          link = EligibilityKeys.START_NEW_REFERRAL,
        )

        Cas3AssessmentStatus.REJECTED -> ServiceResult(
          serviceStatus = ServiceStatus.REJECTED,
          suitableApplicationId = suitableApplicationId,
          action = EligibilityKeys.START_CAS3_REFERRAL,
          link = EligibilityKeys.START_NEW_REFERRAL,
        )

        null -> when (applicationStatus) {
          Cas3ApplicationStatus.IN_PROGRESS -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_SUBMITTED,
            suitableApplicationId = suitableApplicationId,
            link = EligibilityKeys.VIEW_REFERRAL,
          )

          Cas3ApplicationStatus.SUBMITTED,
          Cas3ApplicationStatus.REQUESTED_FURTHER_INFORMATION,
          -> ServiceResult(
            serviceStatus = ServiceStatus.SUBMITTED,
            suitableApplicationId = suitableApplicationId,
            action = EligibilityKeys.WAIT_FOR_CAS3_ASSESSMENT_RESULT,
            link = EligibilityKeys.VIEW_REFERRAL,
          )

          Cas3ApplicationStatus.REJECTED -> ServiceResult(
            serviceStatus = ServiceStatus.REJECTED,
            suitableApplicationId = suitableApplicationId,
            action = EligibilityKeys.START_CAS3_REFERRAL,
            link = EligibilityKeys.START_NEW_REFERRAL,
          )

          null -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_STARTED,
            suitableApplicationId = suitableApplicationId,
            action = EligibilityKeys.START_CAS3_REFERRAL,
            link = EligibilityKeys.START_REFERRAL,
          )
        }
      }
    }
  }
}
