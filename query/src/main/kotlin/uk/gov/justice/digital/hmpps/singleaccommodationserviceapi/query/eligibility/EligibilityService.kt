package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ValidationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2CourtBailRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2HdcRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.Cas2PrisonBailRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3ValidationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.validation.ValidationContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine

@Service
class EligibilityService(
  private val eligibilityOrchestrationService: EligibilityOrchestrationService,
  private val caseRepository: CaseRepository,
  private val cas1EligibilityRuleSet: Cas1EligibilityRuleSet,
  private val cas1SuitabilityRuleSet: Cas1SuitabilityRuleSet,
  private val cas1CompletionRuleSet: Cas1CompletionRuleSet,
  private val cas1ValidationRuleSet: Cas1ValidationRuleSet,
  private val cas3ValidationRuleSet: Cas3ValidationRuleSet,
  private val cas2HdcRuleSet: Cas2HdcRuleSet,
  private val cas2PrisonBailRuleSet: Cas2PrisonBailRuleSet,
  private val cas1ContextUpdater: Cas1ContextUpdater,
  private val validationContextUpdater: ValidationContextUpdater,
  private val cas2CourtBailRuleSet: Cas2CourtBailRuleSet,
  private val cas3EligibilityRuleSet: Cas3EligibilityRuleSet,
  private val cas3SuitabilityRuleSet: Cas3SuitabilityRuleSet,
  private val cas3CompletionRuleSet: Cas3CompletionRuleSet,
  private val cas3ContextUpdater: Cas3ContextUpdater,
  @Qualifier("defaultRulesEngine")
  private val engine: RulesEngine,
) {
  private val treeBuilder = DecisionTreeBuilder(engine)
  private val log = LoggerFactory.getLogger(this::class.java)

  fun getEligibility(crn: String): EligibilityDto {
    log.info("Calculating eligibility for CRN: $crn")
    val data = getDomainData(crn)

    log.info("External Data")
    log.info("CRN: ${data.crn}")
    log.info("Release date: ${data.releaseDate}")
    log.info("Tier: ${data.tier}")
    log.info("Sex code: ${data.sex}")
    log.info("CRS Status: ${data.crsStatus}")
    log.info("DTR Status: ${data.dtrStatus}")
    log.info("Current Accommodation: ${data.currentAccommodation?.name}")
    log.info("Next Accommodation: ${data.nextAccommodation?.name}")

    val cas1 = calculateEligibilityForCas1(data)
    val cas2Hdc = calculateEligibilityForCas2Hdc(data)
    val cas2PrisonBail = calculateEligibilityForCas2PrisonBail(data)
    val cas2CourtBail = calculateEligibilityForCas2CourtBail(data)
    val cas3 = calculateEligibilityForCas3(data)

    val eligibility = EligibilityTransformer.toEligibilityDto(
      crn = crn,
      cas1 = cas1,
      cas2Hdc = cas2Hdc,
      cas2PrisonBail = cas2PrisonBail,
      cas2CourtBail = cas2CourtBail,
      cas3 = cas3,
    )

    log.info("Finished calculating eligibility for CRN: $crn")

    return eligibility
  }

  fun calculateEligibilityForCas1(data: DomainData): ServiceResult {
    log.info("Calculating CAS1 eligibility for CRN: ${data.crn}")

    log.info("CAS1 Data")
    if (data.cas1Application == null) {
      log.info("No CAS1 application")
    } else {
      log.info("Application ID: ${data.cas1Application.id}")
      log.info("Application Status: ${data.cas1Application.applicationStatus}")
      log.info("Placement Status: ${data.cas1Application.placementStatus}")
    }

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
        .ruleSet("Cas1Validation", cas1ValidationRuleSet, validationContextUpdater)
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

    val result = tree.eval(initialContext)

    log.info("Finished CAS1 calculating eligibility for CRN: ${data.crn}")
    logServiceResult(result)

    return result
  }

  private fun logServiceResult(result: ServiceResult) {
    log.info("Service Status: ${result.serviceStatus}")
    log.info("Suitable Application ID: ${result.suitableApplicationId}")
    log.info("Action: ${result.action?.text}")
  }

  fun calculateEligibilityForCas2Hdc(data: DomainData): ServiceResult {
    log.info("Calculating CAS2 HDC eligibility for CRN: ${data.crn}")

    log.info("CAS2 HDC Data")
    if (data.cas2HdcApplication == null) {
      log.info("No CAS2 HDC application")
    } else {
      log.info("Application ID: ${data.cas2HdcApplication.id}")
    }

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

    val result = tree.eval(initialContext)

    log.info("Finished CAS2 HDC calculating eligibility for CRN: ${data.crn}")
    logServiceResult(result)

    return result
  }

  fun calculateEligibilityForCas2CourtBail(data: DomainData): ServiceResult {
    log.info("Calculating CAS2 Court Bail eligibility for CRN: ${data.crn}")

    log.info("CAS2 Court Bail Data")
    if (data.cas2CourtBailApplication == null) {
      log.info("No CAS2 Court Bail application")
    } else {
      log.info("Application ID: ${data.cas2CourtBailApplication.id}")
    }

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

    val result = tree.eval(initialContext)

    log.info("Finished CAS2 Court Bail calculating eligibility for CRN: ${data.crn}")
    logServiceResult(result)

    return result
  }

  fun calculateEligibilityForCas2PrisonBail(data: DomainData): ServiceResult {
    log.info("Calculating CAS2 Prison Bail eligibility for CRN: ${data.crn}")

    log.info("CAS2 Prison Bail Data")
    if (data.cas2PrisonBailApplication == null) {
      log.info("No CAS2 Prison Bail application")
    } else {
      log.info("Application ID: ${data.cas2PrisonBailApplication.id}")
    }

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

    val result = tree.eval(initialContext)

    log.info("Finished CAS2 Prison Bail calculating eligibility for CRN: ${data.crn}")
    logServiceResult(result)

    return result
  }

  fun calculateEligibilityForCas3(data: DomainData): ServiceResult {
    log.info("Calculating CAS3 eligibility for CRN: ${data.crn}")

    log.info("CAS3 Data")
    if (data.cas3Application == null) {
      log.info("No CAS3 application")
    } else {
      log.info("Application ID: ${data.cas3Application.id}")
      log.info("Application Status: ${data.cas3Application.applicationStatus}")
      log.info("Placement Status: ${data.cas3Application.placementStatus}")
    }

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

    val completion =
      treeBuilder
        .ruleSet("Cas3Completion", cas3CompletionRuleSet, cas3ContextUpdater)
        .onPass(confirmed)
        .onFail(suitability)
        .build()

    val tree =
      treeBuilder
        .ruleSet("Cas3Validation", cas3ValidationRuleSet, validationContextUpdater)
        .onPass(completion)
        .onFail(notEligible)
        .build()

    val initialContext =
      EvaluationContext(
        data = data,
        currentResult =
        ServiceResult(
          serviceStatus = ServiceStatus.CONFIRMED,
          suitableApplicationId = data.cas3Application?.id,
        ),
      )

    val result = tree.eval(initialContext)

    log.info("Finished CAS3 calculating eligibility for CRN: ${data.crn}")
    logServiceResult(result)

    return result
  }

  fun getDomainData(crn: String): DomainData {
    val eligibilityOrchestrationDto = eligibilityOrchestrationService.getData(crn)

    val prisonerNumbers = eligibilityOrchestrationDto.cpr.identifiers?.prisonNumbers

    val prisonerData = prisonerNumbers?.let { eligibilityOrchestrationService.getPrisonerData(prisonerNumbers) }

    // read the tier from the db, falling back to api if its not found (to be resolved)
    val tier = caseRepository.findTierScoreByCrn(crn)?.let { Tier.placeholder(it) } ?: eligibilityOrchestrationDto.tier

    return DomainData(
      crn = crn,
      cpr = eligibilityOrchestrationDto.cpr,
      tier = tier,
      prisonerData = prisonerData,
      cas1Application = eligibilityOrchestrationDto.cas1Application,
      cas2HdcApplication = eligibilityOrchestrationDto.cas2HdcApplication,
      cas2PrisonBailApplication = eligibilityOrchestrationDto.cas2PrisonBailApplication,
      cas2CourtBailApplication = eligibilityOrchestrationDto.cas2CourtBailApplication,
    )
  }
}
