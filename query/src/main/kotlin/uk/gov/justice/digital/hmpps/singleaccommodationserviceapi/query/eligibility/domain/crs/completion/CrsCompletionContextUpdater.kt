package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class CrsCompletionContextUpdater : ContextUpdater() {

  override fun toServiceResult(context: EvaluationContext) = ServiceResult(
    serviceStatus = ServiceStatus.NOT_STARTED,
    action = if (context.data.sex == SexCode.M) EligibilityKeys.SUBMIT_CRS_ACCOMMODATION_REFERRAL else EligibilityKeys.SUBMIT_CRS_REFERRAL,
    link = EligibilityKeys.VIEW_REFER_AND_MONITOR,
  )
}
