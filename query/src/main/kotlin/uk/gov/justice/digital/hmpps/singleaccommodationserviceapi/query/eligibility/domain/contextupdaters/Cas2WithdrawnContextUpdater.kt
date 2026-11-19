package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext

@Component
class Cas2WithdrawnContextUpdater : ContextUpdater() {

  val withdrawn = "withdrawn"

  override val outcomes = mapOf(
    withdrawn to ServiceResultSpec(
      serviceStatus = ServiceStatusV2.CAS2_WITHDRAWN,
    ),
  )

  override fun toServiceResult(context: EvaluationContext) = outcome(withdrawn)
}
