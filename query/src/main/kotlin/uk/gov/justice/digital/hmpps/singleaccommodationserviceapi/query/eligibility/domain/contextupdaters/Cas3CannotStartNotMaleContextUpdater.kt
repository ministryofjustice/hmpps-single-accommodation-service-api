package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext

@Component
class Cas3CannotStartNotMaleContextUpdater : ContextUpdater() {
  override val terminalNode = true

  val dtrAndCrs = "dtrAndCrs"
  val crs = "crs"
  val dtr = "dtr"

  override val outcomes = mapOf(
    dtrAndCrs to ServiceResultSpec(
      serviceStatus = ServiceStatusV2.CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS,
      failureReasons = listOf(FailureReason.DTR_REFERRAL_EXPIRED, FailureReason.CRS_NOT_LIVE),
    ),
    crs to ServiceResultSpec(
      serviceStatus = ServiceStatusV2.CAS3_CANNOT_START_YET_SUBMIT_CRS,
      failureReasons = listOf(FailureReason.CRS_NOT_LIVE),
    ),
    dtr to ServiceResultSpec(
      serviceStatus = ServiceStatusV2.CAS3_CANNOT_START_YET_SUBMIT_DTR,
      failureReasons = listOf(FailureReason.DTR_REFERRAL_EXPIRED),
    ),
  )

  override fun toServiceResult(context: EvaluationContext): ServiceResultV2 {
    val currentFailureReasons = context.failureReasons.toSet()
    return outcomes.values
      .single { it.failureReasons.toSet() == currentFailureReasons }
      .toResult()
  }
}
