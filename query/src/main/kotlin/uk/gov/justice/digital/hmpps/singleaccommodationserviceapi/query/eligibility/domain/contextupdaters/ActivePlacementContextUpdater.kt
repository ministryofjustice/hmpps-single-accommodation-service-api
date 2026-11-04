package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext

@Component
class ActivePlacementContextUpdater : ContextUpdater() {

  val arrived = "arrived"
  val notArrived = "notArrived"
  val placementBooked = "placementBooked"
  val placementCancelled = "placementCancelled"

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
  )
  override val terminalNode = true

  override fun toServiceResult(context: EvaluationContext): ServiceResultNew {
    val placementStatus = context.data.cas1Application?.placement?.status

    return when (placementStatus) {
      Cas1PlacementStatus.ARRIVED -> outcome(arrived)
      Cas1PlacementStatus.NOT_ARRIVED -> outcome(notArrived)
      Cas1PlacementStatus.CANCELLED -> outcome(placementCancelled)
      else -> outcome(placementBooked)
    }
  }
}
