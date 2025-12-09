package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.orchestration.EligibilityOrchestrationService

@Service
class EligibilityService(
  private val eligibilityOrchestrationService: EligibilityOrchestrationService,
) {
  private val cas1RuleSet = Cas1RuleSet()

  fun getEligibility(crn: String): EligibilityDto {
    val data = eligibilityOrchestrationService.getData(crn)

    val cas1 = if (data.cas1Application != null) {
      ServiceResult(
        serviceStatus = data.cas1Application.transformToServiceStatus(),
        actions = data.cas1Application.buildActions(),
      )
    } else {
      data.calculateEligibility(cas1RuleSet)
    }

    return EligibilityDto(
      crn,
      cas1,
    )
  }
}
