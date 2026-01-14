package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvalContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSetStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2CourtBailRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2HdcRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2PrisonBailRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine

@Service
class EligibilityService(
  private val eligibilityOrchestrationService: EligibilityOrchestrationService,
  private val cas1EligibilityRuleSet: Cas1EligibilityRuleSet,
  private val cas1SuitabilityRuleSet: Cas1SuitabilityRuleSet,
  private val cas1CompletionRuleSet: Cas1CompletionRuleSet,
  private val cas2HdcRuleSet: Cas2HdcRuleSet,
  private val cas2PrisonBailRuleSet: Cas2PrisonBailRuleSet,
  private val cas2CourtBailRuleSet: Cas2CourtBailRuleSet,
  @Qualifier("defaultRulesEngine")
  private val engine: RulesEngine,
) {

  private val treeBuilder = DecisionTreeBuilder(engine)
  private val cas1ContextUpdater = Cas1ContextUpdater()

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

  fun calculateEligibilityForCas1(data: DomainData): ServiceResult {
    // Build tree declaratively:
    val confirmed = treeBuilder.confirmed()
    val notEligible = treeBuilder.notEligible()

    val eligibility =
      treeBuilder
        .ruleSet("Cas1Eligibility", cas1EligibilityRuleSet, cas1ContextUpdater)
        .onPass(confirmed)
        .onFail(notEligible)
        .build()

    val suitability =
      treeBuilder
        .ruleSet("Cas1Suitability", cas1SuitabilityRuleSet, cas1ContextUpdater)
        .onPass(confirmed)
        .onFail(eligibility) // node above
        .build()

    val tree =
      treeBuilder
        .ruleSet("Cas1Completion", cas1CompletionRuleSet, cas1ContextUpdater)
        .onPass(confirmed)
        .onFail(suitability) // node above
        .build()

    val initialContext =
      EvalContext(
        data = data,
        current =
          ServiceResult(
            serviceStatus = ServiceStatus.CONFIRMED,
            suitableApplicationId = data.cas1Application?.id,
            actions = emptyList()
          )
      )

    return tree.eval(initialContext)
  }

  fun calculateQueue(
    data: DomainData,
    ruleSets: List<RuleSet>,
    ruleSetIndex: Int = 0,
    passServiceResult: ServiceResult = ServiceResult(
      serviceStatus = ServiceStatus.CONFIRMED,
      suitableApplicationId = data.cas1Application?.id,
      actions = listOf(),
    ),
  ): ServiceResult {
    val result = engine.execute(ruleSets[ruleSetIndex], data)
    if(result.ruleSetStatus == RuleSetStatus.PASS) {
      return passServiceResult
    } else {
      val newRuleSetIndex = ruleSetIndex + 1
      return if (newRuleSetIndex == ruleSets.size) {
        ServiceResult(
          serviceStatus = ServiceStatus.NOT_ELIGIBLE,
          suitableApplicationId = null,
          actions = listOf(),
          )
      } else {
        val hasImminentActions = result.actions.any { it.isUpcoming == false }
        calculateQueue(
          data,
          ruleSets,
          newRuleSetIndex,
          ServiceResult(
            serviceStatus = toServiceStatus(data.cas1Application?.applicationStatus, hasImminentActions),
            suitableApplicationId = data.cas1Application?.id,
            actions = result.actions,
          )
        )
      }
    }
  }

  fun calculateEligibilityForCas2Hdc(data: DomainData) = calculateQueue(
    data = data,
    ruleSets = listOf(
      cas2HdcRuleSet,
    ),
  )

  fun calculateEligibilityForCas2CourtBail(data: DomainData) = calculateQueue(
    data = data,
    ruleSets = listOf(
      cas2CourtBailRuleSet,
    ),
  )

  fun calculateEligibilityForCas2PrisonBail(data: DomainData) = calculateQueue(
    data = data,
    ruleSets = listOf(
      cas2PrisonBailRuleSet,
    ),
  )

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
