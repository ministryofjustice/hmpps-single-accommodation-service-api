package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class CrsCompletionContextUpdater(
  @Value($$"${service.commissioned-rehabilitative-services-ui.base-url}") crsUiBaseUrl: String,
) : ContextUpdater() {

  val url = crsUiBaseUrl

  override val description = set("Not started and submit CRS referral")

  val notStartedMale = "notStartedMale"
  val notStartedNonMale = "notStartedNonMale"

  override val outcomes = mapOf(
    notStartedMale to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CRS_NOT_STARTED_ACCOMMODATION_REFERRAL,
      link = EligibilityKeys.VIEW_REFER_AND_MONITOR,
      url = url,
    ),
    notStartedNonMale to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CRS_NOT_STARTED_REFERRAL,
      link = EligibilityKeys.VIEW_REFER_AND_MONITOR,
      url = url,
    ),
  )

  override fun toServiceResult(context: EvaluationContext) = if (context.data.sex == SexCode.M) {
    outcome(notStartedMale)
  } else {
    outcome(notStartedNonMale)
  }
}
