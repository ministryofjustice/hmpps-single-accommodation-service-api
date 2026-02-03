package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ValidationContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ValidationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2CourtBailRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2HdcRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2PrisonBailRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine

@Service
class EligibilityService(
  private val eligibilityOrchestrationService: EligibilityOrchestrationService,
  private val cas1EligibilityRuleSet: Cas1EligibilityRuleSet,
  private val cas1SuitabilityRuleSet: Cas1SuitabilityRuleSet,
  private val cas1CompletionRuleSet: Cas1CompletionRuleSet,
  private val cas1ValidationRuleSet: Cas1ValidationRuleSet,
  private val cas2HdcRuleSet: Cas2HdcRuleSet,
  private val cas2PrisonBailRuleSet: Cas2PrisonBailRuleSet,
  private val cas1ContextUpdater: Cas1ContextUpdater,
  private val cas1ValidationContextUpdater: Cas1ValidationContextUpdater,
  private val cas2CourtBailRuleSet: Cas2CourtBailRuleSet,
  private val cas3EligibilityRuleSet: Cas3EligibilityRuleSet,
  private val cas3SuitabilityRuleSet: Cas3SuitabilityRuleSet,
  private val cas3CompletionRuleSet: Cas3CompletionRuleSet,
  private val cas3ContextUpdater: Cas3ContextUpdater,
  @Qualifier("defaultRulesEngine")
  private val engine: RulesEngine,
) {

  private val treeBuilder = DecisionTreeBuilder(engine)

  fun getEligibility(crn: String): EligibilityDto {
    val data = getDomainData(crn)

    val cas1 = calculateEligibilityForCas1(data)
    val cas2Hdc = calculateEligibilityForCas2Hdc(data)
    val cas2PrisonBail = calculateEligibilityForCas2PrisonBail(data)
    val cas2CourtBail = calculateEligibilityForCas2CourtBail(data)
    val cas3 = calculateEligibilityForCas3(data)

    return EligibilityTransformer.toEligibilityDto(
      crn = crn,
      cas1 = cas1,
      cas2Hdc = cas2Hdc,
      cas2PrisonBail = cas2PrisonBail,
      cas2CourtBail = cas2CourtBail,
      cas3 = cas3,
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

    val completion =
      treeBuilder
        .ruleSet("Cas1Completion", cas1CompletionRuleSet, cas1ContextUpdater)
        .onPass(confirmed)
        .onFail(suitability) // node above
        .build()

    val tree =
      treeBuilder
        .ruleSet("Cas1Validation", cas1ValidationRuleSet, cas1ValidationContextUpdater)
        .onPass(completion)
        .onFail(notEligible)
        .build()

    val initialContext =
      EvaluationContext(
        data = data,
        currentResult =
        ServiceResult(
          serviceStatus = ServiceStatus.CONFIRMED,
          suitableApplicationId = data.cas1Application?.id,
        ),
      )

    return tree.eval(initialContext)
  }

  fun calculateEligibilityForCas2Hdc(data: DomainData): ServiceResult {
    val cas2ContextUpdater = Cas2ContextUpdater(data.cas2HdcApplication?.id)

    // Build tree declaratively:
    val confirmed = treeBuilder.confirmed()
    val notEligible = treeBuilder.notEligible()

    val tree =
      treeBuilder
        .ruleSet("Cas2Hdc", cas2HdcRuleSet, cas2ContextUpdater)
        .onPass(confirmed)
        .onFail(notEligible)
        .build()

    val initialContext =
      EvaluationContext(
        data = data,
        currentResult =
        ServiceResult(
          serviceStatus = ServiceStatus.NOT_ELIGIBLE,
        ),
      )

    return tree.eval(initialContext)
  }

  fun calculateEligibilityForCas2CourtBail(data: DomainData): ServiceResult {
    val cas2ContextUpdater = Cas2ContextUpdater(data.cas2CourtBailApplication?.id)

    // Build tree declaratively:
    val confirmed = treeBuilder.confirmed()
    val notEligible = treeBuilder.notEligible()

    val tree =
      treeBuilder
        .ruleSet("Cas2CourtBail", cas2CourtBailRuleSet, cas2ContextUpdater)
        .onPass(confirmed)
        .onFail(notEligible) // node above
        .build()

    val initialContext =
      EvaluationContext(
        data = data,
        currentResult =
        ServiceResult(
          serviceStatus = ServiceStatus.NOT_ELIGIBLE,
        ),
      )

    return tree.eval(initialContext)
  }

  fun calculateEligibilityForCas2PrisonBail(data: DomainData): ServiceResult {
    val cas2ContextUpdater = Cas2ContextUpdater(data.cas2PrisonBailApplication?.id)

    // Build tree declaratively:
    val confirmed = treeBuilder.confirmed()
    val notEligible = treeBuilder.notEligible()

    val tree =
      treeBuilder
        .ruleSet("Cas2PrisonBail", cas2PrisonBailRuleSet, cas2ContextUpdater)
        .onPass(confirmed)
        .onFail(notEligible) // node above
        .build()

    val initialContext =
      EvaluationContext(
        data = data,
        currentResult =
        ServiceResult(
          serviceStatus = ServiceStatus.NOT_ELIGIBLE,
        ),
      )

    return tree.eval(initialContext)
  }

  fun calculateEligibilityForCas3(data: DomainData): ServiceResult {
    val confirmed = treeBuilder.confirmed()
    val notEligible = treeBuilder.notEligible()

    val eligibility =
      treeBuilder
        .ruleSet("Cas3Eligibility", cas3EligibilityRuleSet, cas3ContextUpdater)
        .onPass(confirmed)
        .onFail(notEligible)
        .build()

    val suitability =
      treeBuilder
        .ruleSet("Cas3Suitability", cas3SuitabilityRuleSet, cas3ContextUpdater)
        .onPass(confirmed)
        .onFail(eligibility)
        .build()

    val tree =
      treeBuilder
        .ruleSet("Cas3Completion", cas3CompletionRuleSet, cas3ContextUpdater)
        .onPass(confirmed)
        .onFail(suitability)
        .build()

    val initialContext =
      EvaluationContext(
        data = data,
        currentResult =
          ServiceResult(
            serviceStatus = ServiceStatus.CONFIRMED,
            suitableApplicationId = data.cas3Application?.id,
          )
      )

    return tree.eval(initialContext)
  }

  fun getDomainData(crn: String): DomainData {
    val eligibilityOrchestrationDto = eligibilityOrchestrationService.getData(crn)

    val prisonerNumbers = eligibilityOrchestrationDto.cpr.identifiers?.prisonNumbers ?: error("No prisoner numbers found for crn: $crn")

    val prisonerData = eligibilityOrchestrationService.getPrisonerData(prisonerNumbers)

    return DomainData(
      crn = crn,
      cpr = eligibilityOrchestrationDto.cpr,
      tier = eligibilityOrchestrationDto.tier,
      prisonerData = prisonerData,
      cas1Application = eligibilityOrchestrationDto.cas1Application,
      cas2HdcApplication = eligibilityOrchestrationDto.cas2HdcApplication,
      cas2PrisonBailApplication = eligibilityOrchestrationDto.cas2PrisonBailApplication,
      cas2CourtBailApplication = eligibilityOrchestrationDto.cas2CourtBailApplication,
    )
  }
}
