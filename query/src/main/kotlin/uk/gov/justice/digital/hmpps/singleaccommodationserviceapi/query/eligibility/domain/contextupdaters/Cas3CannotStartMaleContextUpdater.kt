package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext

@Component
class Cas3CannotStartMaleContextUpdater : ContextUpdater() {
  override val terminalNode = true

  val dtrAndCrsAccommodation = "dtrAndCrsAccommodation"
  val crsAccommodation = "crsAccommodation"
  val dtr = "dtr"

  override val outcomes = mapOf(
    dtrAndCrsAccommodation to ServiceResultSpec(
      serviceStatus = ServiceStatusV2.CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS_ACCOMMODATION,
      failureReasons = listOf(FailureReason.DTR_REFERRAL_EXPIRED, FailureReason.CRS_NOT_LIVE),
    ),
    crsAccommodation to ServiceResultSpec(
      serviceStatus = ServiceStatusV2.CAS3_CANNOT_START_YET_SUBMIT_CRS_ACCOMMODATION,
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
