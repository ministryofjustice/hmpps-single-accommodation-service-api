package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas2StartContextUpdater : ContextUpdater() {

  val notStarted = "notStarted"

  override val outcomes = mapOf(
    notStarted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_NOT_STARTED,
    ),
  )

  override fun toServiceResult(context: EvaluationContext) = outcome(notStarted)
}
