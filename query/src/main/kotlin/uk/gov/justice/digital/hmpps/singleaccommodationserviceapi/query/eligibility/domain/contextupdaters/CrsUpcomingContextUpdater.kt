package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.SexAndAccommodationData

@Component
class CrsUpcomingContextUpdater : ContextUpdater<SexAndAccommodationData>() {
  override val inputFields = setOf(DomainDataField.SEX, DomainDataField.CURRENT_ACCOMMODATION_END_DATE)

  val upcomingMale = "upcomingMale"
  val upcomingNonMale = "upcomingNonMale"

  override val outcomes = mapOf(
    upcomingMale to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CRS_UPCOMING_ACCOMMODATION_REFERRAL,
    ),
    upcomingNonMale to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CRS_UPCOMING_REFERRAL,
    ),
  )

  override fun toServiceResult(context: EvaluationContext<SexAndAccommodationData>): ServiceResultNew {
    val key = if (context.data.sex == SexCode.M) upcomingMale else upcomingNonMale

    return outcome(
      key = key,
      actionStartDate = context.data.currentAccommodation!!.endDate!!.minusWeeks(12),
    )
  }
}
