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

  val notArrived = "notArrived"
  val placementCancelled = "placementCancelled"

  override val outcomes = mapOf(
    notArrived to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_NOT_ARRIVED,
    ),
    placementCancelled to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_CANCELLED,
    ),
  )
  override val terminalNode = true

  override fun toServiceResult(context: EvaluationContext): ServiceResultNew {
    val placementStatus = context.data.cas1Application?.placement?.status

    return when (placementStatus) {
      Cas1PlacementStatus.NOT_ARRIVED -> outcome(notArrived)
      else -> outcome(placementCancelled)
    }
  }
}
