package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

object Cas3ServiceResultTransformer {
  fun toCas3ServiceResult(data: DomainData, clock: Clock): ServiceResult {
    println("Cas3ServiceResultTransformer.toCas3ServiceResult")
    val applicationStatus = data.cas3Application?.applicationStatus
    val assessmentStatus = data.cas3Application?.assessmentStatus
    val bookingStatus = data.cas3Application?.bookingStatus
    val suitableApplicationId = data.cas3Application?.id
    val releaseDate = data.releaseDate

    val notEligible = ServiceResult(
      serviceStatus = ServiceStatus.NOT_ELIGIBLE,
      suitableApplicationId = suitableApplicationId,
      action = null,
      link = null,
    )

    if (releaseDate == null) {
      return notEligible
    }

    val today = LocalDate.now(clock)
    val isWithinOneYear = !releaseDate.isAfter(today.plusYears(1))

    if (!isWithinOneYear) {
      return ServiceResult(
        serviceStatus = ServiceStatus.UPCOMING,
        suitableApplicationId = suitableApplicationId,
        action = buildStartCas3ReferralReferralAction(releaseDate, today),
        link = null,
      )
    }

    println("Cas3ServiceResultTransformer.toCas3ServiceResult: applicationStatus=$applicationStatus, assessmentStatus=$assessmentStatus, bookingStatus=$bookingStatus")

    return when (applicationStatus) {
      Cas3ApplicationStatus.IN_PROGRESS -> when (assessmentStatus) {
        Cas3AssessmentStatus.READY_TO_PLACE -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.SUBMITTED,
            suitableApplicationId = suitableApplicationId,
            action = "Wait for CAS3 assessment result",
            link = "View referral",
          )
        }

        Cas3AssessmentStatus.UNALLOCATED -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.SUBMITTED,
            suitableApplicationId = suitableApplicationId,
            action = "Wait for CAS3 assessment result",
            link = "View referral",
          )
        }

        Cas3AssessmentStatus.IN_REVIEW -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.SUBMITTED,
            suitableApplicationId = suitableApplicationId,
            action = "Wait for CAS3 assessment result",
            link = "View referral",
          )
        }

        Cas3AssessmentStatus.CLOSED -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_STARTED,
            suitableApplicationId = suitableApplicationId,
            link = "Start new referral",
          )
        }

        Cas3AssessmentStatus.REJECTED -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.REJECTED,
            suitableApplicationId = suitableApplicationId,
            link = "Start new referral",
          )
        }

        null -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_SUBMITTED,
            suitableApplicationId = suitableApplicationId,
            link = "Continue referral",
          )
        }
      }

      Cas3ApplicationStatus.SUBMITTED -> when (assessmentStatus) {
        Cas3AssessmentStatus.READY_TO_PLACE -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.SUBMITTED,
            suitableApplicationId = suitableApplicationId,
            action = "Wait for CAS3 assessment result",
            link = "View referral",
          )
        }

        Cas3AssessmentStatus.UNALLOCATED -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.SUBMITTED,
            suitableApplicationId = suitableApplicationId,
            action = "Wait for CAS3 assessment result",
            link = "View referral",
          )
        }

        Cas3AssessmentStatus.IN_REVIEW -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.SUBMITTED,
            suitableApplicationId = suitableApplicationId,
            action = "Wait for CAS3 assessment result",
            link = "View referral",
          )
        }

        Cas3AssessmentStatus.CLOSED -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_STARTED,
            suitableApplicationId = suitableApplicationId,
            link = "Start new referral",
          )
        }

        Cas3AssessmentStatus.REJECTED -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.REJECTED,
            suitableApplicationId = suitableApplicationId,
            link = "Start new referral",
          )
        }

        null -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.SUBMITTED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )
        }
      }

      Cas3ApplicationStatus.REQUESTED_FURTHER_INFORMATION -> when (assessmentStatus) {
        Cas3AssessmentStatus.READY_TO_PLACE -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.SUBMITTED,
            suitableApplicationId = suitableApplicationId,
            action = "Wait for CAS3 assessment result",
            link = "View referral",
          )
        }

        Cas3AssessmentStatus.UNALLOCATED -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.SUBMITTED,
            suitableApplicationId = suitableApplicationId,
            action = "Wait for CAS3 assessment result",
            link = "View referral",
          )
        }

        Cas3AssessmentStatus.IN_REVIEW -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.SUBMITTED,
            suitableApplicationId = suitableApplicationId,
            action = "Wait for CAS3 assessment result",
            link = "View referral",
          )
        }

        Cas3AssessmentStatus.CLOSED -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_STARTED,
            suitableApplicationId = suitableApplicationId,
            link = "Start new referral",
          )
        }

        Cas3AssessmentStatus.REJECTED -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.REJECTED,
            suitableApplicationId = suitableApplicationId,
            link = "Start new referral",
          )
        }

        null -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> notEligible
        }
      }

      Cas3ApplicationStatus.REJECTED -> when (assessmentStatus) {
        Cas3AssessmentStatus.READY_TO_PLACE -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.SUBMITTED,
            suitableApplicationId = suitableApplicationId,
            action = "Wait for CAS3 assessment result",
            link = "View referral",
          )
        }

        Cas3AssessmentStatus.UNALLOCATED -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.SUBMITTED,
            suitableApplicationId = suitableApplicationId,
            action = "Wait for CAS3 assessment result",
            link = "View referral",
          )
        }

        Cas3AssessmentStatus.IN_REVIEW -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.SUBMITTED,
            suitableApplicationId = suitableApplicationId,
            action = "Wait for CAS3 assessment result",
            link = "View referral",
          )
        }

        Cas3AssessmentStatus.CLOSED -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_STARTED,
            suitableApplicationId = suitableApplicationId,
            link = "Start new referral",
          )
        }

        Cas3AssessmentStatus.REJECTED -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.REJECTED,
            suitableApplicationId = suitableApplicationId,
            link = "Start new referral",
          )
        }

        null -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.REJECTED,
            suitableApplicationId = suitableApplicationId,
            link = "Start new referral",
          )
        }
      }

      null -> when (assessmentStatus) {
        Cas3AssessmentStatus.READY_TO_PLACE -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.SUBMITTED,
            suitableApplicationId = suitableApplicationId,
            action = "Wait for CAS3 assessment result",
            link = "View referral",
          )
        }

        Cas3AssessmentStatus.UNALLOCATED -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.SUBMITTED,
            suitableApplicationId = suitableApplicationId,
            action = "Wait for CAS3 assessment result",
            link = "View referral",
          )
        }

        Cas3AssessmentStatus.IN_REVIEW -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> notEligible
        }

        Cas3AssessmentStatus.CLOSED -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_STARTED,
            suitableApplicationId = suitableApplicationId,
            link = "Start new referral",
          )
        }

        Cas3AssessmentStatus.REJECTED -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> ServiceResult(
            serviceStatus = ServiceStatus.REJECTED,
            suitableApplicationId = suitableApplicationId,
            link = "Start new referral",
          )
        }

        null -> when (bookingStatus) {
          Cas3PlacementStatus.ARRIVED -> notEligible
          Cas3PlacementStatus.PROVISIONAL -> ServiceResult(
            serviceStatus = ServiceStatus.BEDSPACE_OFFERED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CONFIRMED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.NOT_MINUS_ARRIVED -> ServiceResult(
            serviceStatus = ServiceStatus.NOT_ARRIVED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.DEPARTED -> notEligible
          Cas3PlacementStatus.CANCELLED -> ServiceResult(
            serviceStatus = ServiceStatus.BOOKING_CANCELLED,
            suitableApplicationId = suitableApplicationId,
            link = "View referral",
          )

          Cas3PlacementStatus.CLOSED -> notEligible
          null -> if (releaseDate.isBefore(today)) {
            notEligible
          } else {
            ServiceResult(
              serviceStatus = ServiceStatus.NOT_STARTED,
              action = "Start CAS3 referral",
              link = "Start referral",
            )
          }
        }
      }
    }
  }

  private fun buildStartCas3ReferralReferralAction(releaseDate: LocalDate, today: LocalDate): String {
    val dateToStartReferral = releaseDate.minusYears(1)
    val daysUntilReferralMustStart = DAYS.between(today, dateToStartReferral).toInt()
    val formattedMonth = dateToStartReferral.month.name.lowercase()
      .replaceFirstChar { it.uppercase() }

    val formattedDate = "(${dateToStartReferral.dayOfMonth} $formattedMonth ${dateToStartReferral.year})"

    return when {
      daysUntilReferralMustStart > 1
      -> "${ActionKeys.START_REFERRAL} in $daysUntilReferralMustStart days $formattedDate"

      daysUntilReferralMustStart < 1 -> ActionKeys.START_REFERRAL

      else -> "${ActionKeys.START_REFERRAL} in 1 day $formattedDate"
    }
  }
}
