package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.SexData

@Component
class CrsStartContextUpdater(
  @Value($$"${service.commissioned-rehabilitative-services-ui.base-url}") crsUiBaseUrl: String,
) : ContextUpdater<SexData>() {
  override val inputFields = setOf(DomainDataField.SEX)

  val url = crsUiBaseUrl

  val notStartedMale = "notStartedMale"
  val notStartedNonMale = "notStartedNonMale"

  override val outcomes = mapOf(
    notStartedMale to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CRS_NOT_STARTED_ACCOMMODATION_REFERRAL,
      url = url,
    ),
    notStartedNonMale to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CRS_NOT_STARTED_REFERRAL,
      url = url,
    ),
  )

  override fun toServiceResult(context: EvaluationContext<SexData>) = if (context.data.sex == SexCode.M) {
    outcome(notStartedMale)
  } else {
    outcome(notStartedNonMale)
  }
}
