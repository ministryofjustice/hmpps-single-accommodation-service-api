package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class CrsCompletionContextUpdater(
  @Value($$"${frontend.commissioned-rehabilitative-services.find-interventions}") findInterventionsUrl: String,
) : ContextUpdater() {

  val notStartedMale = "notStartedMale"
  val notStartedNonMale = "notStartedNonMale"

  override val outcomes = mapOf(
    notStartedMale to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CRS_NOT_STARTED_ACCOMMODATION_REFERRAL,
      url = findInterventionsUrl,
    ),
    notStartedNonMale to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CRS_NOT_STARTED_REFERRAL,
      url = findInterventionsUrl,
    ),
  )

  override fun toServiceResult(context: EvaluationContext) = if (context.data.sex == SexCode.M) {
    outcome(notStartedMale)
  } else {
    outcome(notStartedNonMale)
  }
}
