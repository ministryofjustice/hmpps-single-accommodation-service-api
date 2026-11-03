package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.placement

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class PlacementContextUpdater : ContextUpdater() {

  val arrived = "arrived"
  val notArrived = "notArrived"
  val placementBooked = "placementBooked"
  val placementCancelled = "placementCancelled"
  val placementRequestNotStarted = "placementRequestNotStarted"
  val submitted = "submitted"

  override val outcomes = mapOf(
    arrived to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_ARRIVED,
    ),
    notArrived to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_NOT_ARRIVED,
    ),
    placementBooked to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_BOOKED,
    ),
    placementCancelled to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_CANCELLED,
    ),
    placementRequestNotStarted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_REQUEST_NOT_STARTED,
    ),
  )
  override val terminalNode = true

  override fun toServiceResult(context: EvaluationContext): ServiceResultNew {
    val placementStatus = context.data.cas1Application?.placement?.status

    return when (placementStatus) {
      Cas1PlacementStatus.ARRIVED -> outcome(arrived)
      Cas1PlacementStatus.NOT_ARRIVED -> outcome(notArrived)
      Cas1PlacementStatus.CANCELLED -> outcome(placementCancelled)
      Cas1PlacementStatus.UPCOMING -> outcome(placementBooked)
      else -> outcome(placementRequestNotStarted)
    }
  }
}
