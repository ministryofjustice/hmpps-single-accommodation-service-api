package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.arrived

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas2ArrivedContextUpdater : ContextUpdater() {
  override val terminalNode = true

  val arrived = "arrived"

  override val outcomes = mapOf(
    arrived to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_ARRIVED,
    ),
  )

  override fun toServiceResult(context: EvaluationContext) = outcome(arrived)
}
