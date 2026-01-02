package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine

@Service
class EligibilityService(
  private val eligibilityOrchestrationService: EligibilityOrchestrationService,
  private val cas1RuleSet: Cas1RuleSet,
  @Qualifier("defaultRulesEngine")
  private val engine: RulesEngine,
) {

  fun getEligibility(crn: String): EligibilityDto {
    val data = getDomainData(crn)

    val cas1 = calculateEligibilityForCas1(data)

    return toEligibilityDto(crn, cas1)
  }

  fun calculateEligibilityForCas1(data: DomainData) = if (data.cas1Application != null) {
    ServiceResult(
      serviceStatus = toServiceStatus(data.cas1Application),
      actions = buildActions(data.cas1Application),
    )
  } else {
    engine.execute(cas1RuleSet, data)
  }

  fun getDomainData(crn: String): DomainData {
    val eligibilityOrchestrationDto = eligibilityOrchestrationService.getData(crn)

    val prisonerNumbers = eligibilityOrchestrationDto.cpr.identifiers?.prisonNumbers ?: error("No prisoner numbers found for crn: $crn")

    val prisonerData = eligibilityOrchestrationService.getPrisonerData(prisonerNumbers)

    return DomainData(
      crn,
      eligibilityOrchestrationDto.cpr,
      eligibilityOrchestrationDto.tier,
      prisonerData,
      eligibilityOrchestrationDto.cas1Application,
    )
  }
}
