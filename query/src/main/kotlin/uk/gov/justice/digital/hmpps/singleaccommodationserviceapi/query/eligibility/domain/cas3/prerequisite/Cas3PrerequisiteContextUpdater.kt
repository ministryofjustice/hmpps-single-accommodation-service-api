package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas3PrerequisiteContextUpdater : ContextUpdater() {

  override val description = set("Cannot start yet from outstanding DTR/CRS")

  val dtrAndCrsAccommodation = "dtrAndCrsAccommodation"
  val dtrAndCrs = "dtrAndCrs"
  val crsAccommodation = "crsAccommodation"
  val crs = "crs"
  val dtr = "dtr"

  override val outcomes = mapOf(
    dtrAndCrsAccommodation to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS_ACCOMMODATION,
    ),
    dtrAndCrs to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS,
    ),
    crsAccommodation to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS3_CANNOT_START_YET_SUBMIT_CRS_ACCOMMODATION,
    ),
    crs to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS3_CANNOT_START_YET_SUBMIT_CRS,
    ),
    dtr to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS3_CANNOT_START_YET_SUBMIT_DTR,
    ),
  )

  override fun toServiceResult(context: EvaluationContext): ServiceResultNew {
    val currentFailureReasons = context.currentResult.failureReasons
    val crsOutstandingMale = FailureReason.CRS_NOT_SUBMITTED_MALE in currentFailureReasons
    val crsOutstandingNonMale = FailureReason.CRS_NOT_SUBMITTED_NON_MALE in currentFailureReasons
    val dtrOutstanding = FailureReason.DTR_REFERRAL_EXPIRED in currentFailureReasons

    val key = when {
      dtrOutstanding && crsOutstandingMale -> dtrAndCrsAccommodation
      dtrOutstanding && crsOutstandingNonMale -> dtrAndCrs
      crsOutstandingMale -> crsAccommodation
      crsOutstandingNonMale -> crs
      else -> dtr
    }
    return outcome(key).copy(failureReasons = context.currentResult.failureReasons)
  }
}
