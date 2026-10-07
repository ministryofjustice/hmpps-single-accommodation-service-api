package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas1SuitabilityContextUpdater : ContextUpdater() {

  override val description = set("status from application")

  val notSubmitted = "notSubmitted"
  val applicationRejected = "applicationRejected"
  val notStarted = "notStarted"

  override val outcomes = mapOf(
    notSubmitted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_NOT_SUBMITTED,
    ),
    applicationRejected to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_APPLICATION_REJECTED,
    ),
    notStarted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS1_NOT_STARTED,
    ),
  )

  override fun toServiceResult(context: EvaluationContext): ServiceResultNew {
    val applicationStatus = context.data.cas1Application?.application?.status

    return when (applicationStatus) {
      Cas1ApplicationStatus.STARTED -> outcome(notSubmitted)
      Cas1ApplicationStatus.REJECTED -> outcome(applicationRejected)
      else -> outcome(notStarted)
    }
  }
}
