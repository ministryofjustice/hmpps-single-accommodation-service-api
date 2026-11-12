package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext

@Component
class Cas3NotStartedContextUpdater : ContextUpdater() {

  val notStarted = "notStarted"

  override val outcomes = mapOf(
    notStarted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS3_NOT_STARTED,
    ),
  )

  override fun toServiceResult(context: EvaluationContext) = outcome(notStarted)
}
