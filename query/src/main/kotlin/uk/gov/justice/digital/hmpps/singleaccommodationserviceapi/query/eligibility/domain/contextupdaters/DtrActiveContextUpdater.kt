package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DtrReferralData

@Component
class DtrActiveContextUpdater : ContextUpdater<DtrReferralData>() {
  override val inputFields = setOf(DomainDataField.DTR_STATUS)
  override val terminalNode = true

  val notAccepted = "notAccepted"
  val submitted = "submitted"
  val accepted = "accepted"

  override val outcomes = mapOf(
    accepted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.DTR_ACCEPTED,
    ),
    notAccepted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.DTR_NOT_ACCEPTED,
    ),
    submitted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.DTR_SUBMITTED,
    ),
  )

  override fun toServiceResult(context: EvaluationContext<DtrReferralData>) = when (context.data.dutyToRefer?.status) {
    DtrStatus.NOT_ACCEPTED -> outcome(notAccepted)
    DtrStatus.ACCEPTED -> outcome(accepted)
    else -> outcome(submitted)
  }
}
