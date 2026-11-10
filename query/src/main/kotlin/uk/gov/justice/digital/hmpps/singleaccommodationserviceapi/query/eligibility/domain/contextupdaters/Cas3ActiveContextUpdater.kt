package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas3ApplicationData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField

@Component
class Cas3ActiveContextUpdater : ContextUpdater<Cas3ApplicationData>() {
  override val inputFields = setOf(DomainDataField.CAS3_BOOKING_STATUS)
  override val terminalNode = true

  val bedspaceOffered = "bedspaceOffered"
  val bookingConfirmed = "bookingConfirmed"
  val notArrived = "notArrived"
  val bedspaceCancelled = "bedspaceCancelled"
  val submitted = "submitted"
  val arrived = "arrived"

  override val outcomes = mapOf(
    bedspaceOffered to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS3_BEDSPACE_OFFERED,
    ),
    bookingConfirmed to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS3_BOOKING_CONFIRMED,
    ),
    notArrived to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS3_NOT_ARRIVED,
    ),
    bedspaceCancelled to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS3_BOOKING_CANCELLED,
    ),
    submitted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS3_SUBMITTED,
    ),
    arrived to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS3_ARRIVED,
    ),
  )

  override fun toServiceResult(context: EvaluationContext<Cas3ApplicationData>): ServiceResultNew {
    val bookingStatus = context.data.cas3Application?.submittedApplication?.latestBooking?.status
    return when (bookingStatus) {
      Cas3BookingStatus.PROVISIONAL -> outcome(bedspaceOffered)
      Cas3BookingStatus.CONFIRMED -> outcome(bookingConfirmed)
      Cas3BookingStatus.NOT_MINUS_ARRIVED -> outcome(notArrived)
      Cas3BookingStatus.CANCELLED -> outcome(bedspaceCancelled)
      Cas3BookingStatus.ARRIVED -> outcome(arrived)
      else -> outcome(submitted)
    }
  }
}
