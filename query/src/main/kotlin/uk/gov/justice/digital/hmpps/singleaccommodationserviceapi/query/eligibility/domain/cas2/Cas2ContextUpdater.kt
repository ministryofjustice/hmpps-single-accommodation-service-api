package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater

class Cas2ContextUpdater(
  private val applicationId: java.util.UUID?
) : ContextUpdater {
  override fun update(context: EvaluationContext): EvaluationContext {
    val updatedServiceResult = ServiceResult(
      serviceStatus = ServiceStatus.CONFIRMED,
      suitableApplicationId = applicationId,
    )
    return context.copy(currentResult = updatedServiceResult)
  }
}