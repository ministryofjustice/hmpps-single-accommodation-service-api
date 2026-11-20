package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext

@Component
class Cas3NotSubmittedContextUpdater : ContextUpdater() {

  val notSubmitted = "notSubmitted"

  override val outcomes = mapOf(
    notSubmitted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS3_NOT_SUBMITTED,
    ),
  )

  override fun toServiceResult(context: EvaluationContext) = outcome(notSubmitted)
}
