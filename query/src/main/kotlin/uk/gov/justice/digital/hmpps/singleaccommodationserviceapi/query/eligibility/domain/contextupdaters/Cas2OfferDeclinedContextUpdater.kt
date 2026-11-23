package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext

@Component
class Cas2OfferDeclinedContextUpdater : ContextUpdater() {

  val offerDeclined = "offerDeclined"

  override val outcomes = mapOf(
    offerDeclined to ServiceResultSpec(
      serviceStatus = ServiceStatusV2.CAS2_OFFER_DECLINED_OR_WITHDRAWN,
    ),
  )

  override fun toServiceResult(context: EvaluationContext) = outcome(offerDeclined)
}
