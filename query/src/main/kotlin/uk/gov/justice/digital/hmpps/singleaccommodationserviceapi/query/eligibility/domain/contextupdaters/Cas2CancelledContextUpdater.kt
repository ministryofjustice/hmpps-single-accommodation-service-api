package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext

@Component
class Cas2CancelledContextUpdater : ContextUpdater() {

  val cancelled = "cancelled"

  override val outcomes = mapOf(
    cancelled to ServiceResultSpec(
      serviceStatus = ServiceStatusV2.CAS2_CANCELLED,
    ),
  )

  override fun toServiceResult(context: EvaluationContext) = outcome(cancelled)
}
