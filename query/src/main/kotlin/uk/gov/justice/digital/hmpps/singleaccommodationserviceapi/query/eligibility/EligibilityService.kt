package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2CourtBailRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2HdcRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2PrisonBailRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine

@Service
class EligibilityService(
  private val eligibilityOrchestrationService: EligibilityOrchestrationService,
  private val cas1RuleSet: Cas1RuleSet,
  private val cas2HdcRuleSet: Cas2HdcRuleSet,
  private val cas2PrisonBailRuleSet: Cas2PrisonBailRuleSet,
  private val cas2CourtBailRuleSet: Cas2CourtBailRuleSet,

  @Qualifier("defaultRulesEngine")
  private val engine: RulesEngine,
) {

  fun getEligibility(crn: String): EligibilityDto {
    val data = getDomainData(crn)

    val cas1 = calculateEligibilityForCas1(data)
    val cas2Hdc = calculateEligibilityForCas2Hdc(data)
    val cas2PrisonBail = calculateEligibilityForCas2PrisonBail(data)
    val cas2CourtBail = calculateEligibilityForCas2CourtBail(data)

    return toEligibilityDto(
      crn = crn,
      cas1 = cas1,
      cas2Hdc = cas2Hdc,
      cas2PrisonBail = cas2PrisonBail,
      cas2CourtBail = cas2CourtBail,
      cas3 = null
    )
  }

  fun calculateEligibilityForCas1(data: DomainData) = if (data.cas1Application != null) {
    ServiceResult(
      serviceStatus = toServiceStatus(data.cas1Application),
      actions = buildActions(data.cas1Application),
    )
  } else {
    engine.execute(cas1RuleSet, data)
  }

  fun calculateEligibilityForCas2Hdc(data: DomainData) = if (data.cas2HdcApplication != null) {
    ServiceResult(
      serviceStatus = toServiceStatus(data.cas2HdcApplication),
      actions = buildActions(data.cas2HdcApplication),
    )
  } else {
    engine.execute(cas2HdcRuleSet, data)
  }

  fun calculateEligibilityForCas2CourtBail(data: DomainData) = if (data.cas2CourtBailApplication != null) {
    ServiceResult(
      serviceStatus = toServiceStatus(data.cas2CourtBailApplication),
      actions = buildActions(data.cas2CourtBailApplication),
    )
  } else {
    engine.execute(cas2CourtBailRuleSet, data)
  }

  fun calculateEligibilityForCas2PrisonBail(data: DomainData) = if (data.cas2PrisonBailApplication != null) {
    ServiceResult(
      serviceStatus = toServiceStatus(data.cas2PrisonBailApplication),
      actions = buildActions(data.cas2PrisonBailApplication),
    )
  } else {
    engine.execute(cas2PrisonBailRuleSet, data)
  }

  fun getDomainData(crn: String): DomainData {
    val eligibilityOrchestrationDto = eligibilityOrchestrationService.getData(crn)

    val prisonerNumbers = eligibilityOrchestrationDto.cpr.identifiers?.prisonNumbers ?: error("No prisoner numbers found for crn: $crn")

    val prisonerData = eligibilityOrchestrationService.getPrisonerData(prisonerNumbers)

    return DomainData(
      crn = crn,
      cpr = eligibilityOrchestrationDto.cpr,
      tier = eligibilityOrchestrationDto.tier,
      prisonerData =  prisonerData,
      cas1Application = eligibilityOrchestrationDto.cas1Application,
      cas2HdcApplication = eligibilityOrchestrationDto.cas2HdcApplication,
      cas2PrisonBailApplication = eligibilityOrchestrationDto.cas2PrisonBailApplication,
      cas2CourtBailApplication = eligibilityOrchestrationDto.cas2CourtBailApplication,
    )
  }
}
