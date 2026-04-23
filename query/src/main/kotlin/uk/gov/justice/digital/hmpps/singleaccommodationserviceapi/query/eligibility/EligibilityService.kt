package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion.Cas1CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.Cas1EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.validation.Cas1ValidationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion.Cas3CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.Cas3EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.validation.Cas3ValidationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.CommonContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine

@Service
class EligibilityService(
  private val accommodationQueryService: AccommodationQueryService,
  private val eligibilityOrchestrationService: EligibilityOrchestrationService,
  private val cas1EligibilityRuleSet: Cas1EligibilityRuleSet,
  private val cas1SuitabilityRuleSet: Cas1SuitabilityRuleSet,
  private val cas1CompletionRuleSet: Cas1CompletionRuleSet,
  private val cas1ValidationRuleSet: Cas1ValidationRuleSet,
  private val cas3ValidationRuleSet: Cas3ValidationRuleSet,
  private val cas1ContextUpdater: Cas1ContextUpdater,
  private val commonContextUpdater: CommonContextUpdater,
  private val cas3EligibilityRuleSet: Cas3EligibilityRuleSet,
  private val cas3CompletionRuleSet: Cas3CompletionRuleSet,
  private val cas3ContextUpdater: Cas3ContextUpdater,
  @Qualifier("defaultRulesEngine")
  private val engine: RulesEngine,
  private val cas3SuitabilityRuleSet: Cas3SuitabilityRuleSet,
) {
  private val treeBuilder = DecisionTreeBuilder(engine)
  private val log = LoggerFactory.getLogger(this::class.java)

  fun getEligibility(personDto: PersonDto, caseEntity: CaseEntity?): EligibilityDto {
    log.info("Calculating eligibility for CRN: ${personDto.crn} from the sas_case table")
    val data = DomainData(
      personDto,
      caseEntity,
    )
    return getEligibility(data)
  }

  fun getEligibility(crn: String): EligibilityDto {
    log.info("Calculating eligibility for CRN: $crn using external APIs")
    val data = getDomainData(crn)
    return getEligibility(data)
  }

  fun getEligibility(data: DomainData): EligibilityDto {
    log.debug(
      "Eligibility input data: crn={}, currentAccommodation.?endDate={}, tierScore={}, sex={}, crsStatus={}, dtrStatus={}, currentAccommodationIsPrisonCas1Cas2orCas2v2={}, hasNextAccommodation={}",
      data.crn,
      data.currentAccommodation?.endDate,
      data.tierScore,
      data.sex,
      data.crsStatus,
      data.dtrStatus,
      data.currentAccommodation?.isPrisonCas1Cas2OrCas2v2,
      data.hasNextAccommodation,
    )

    val cas1 = calculateEligibilityForCas1(data)
    val cas3 = calculateEligibilityForCas3(data)

    return EligibilityTransformer.toEligibilityDto(
      crn = data.crn,
      cas1 = cas1,
      cas3 = cas3,
    ).also { log.info("Finished calculating eligibility for CRN: ${data.crn}") }
  }

  fun calculateEligibilityForCas1(data: DomainData): ServiceResult {
    log.info("Calculating CAS1 eligibility for CRN: ${data.crn}")

    data.cas1Application?.let {
      log.info(
        "CAS1 Data received: id={}, applicationStatus={}, requestForPlacementStatus={}, placementStatus={}",
        data.cas1Application.id,
        data.cas1Application.applicationStatus,
        data.cas1Application.requestForPlacementStatus,
        data.cas1Application.placementStatus,
      )
    } ?: log.info("CAS1 Data received: No CAS1 application")

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
        .ruleSet("Cas1Validation", cas1ValidationRuleSet, commonContextUpdater)
        .onPass(completion)
        .onFail(notEligible)
        .build()

    val initialContext =
      EvaluationContext(
        data = data,
        currentResult =
        ServiceResult(
          serviceStatus = ServiceStatus.PLACEMENT_BOOKED,
          suitableApplicationId = data.cas1Application?.id,
          link = EligibilityKeys.VIEW_APPLICATION,
        ),
      )

    return tree.eval(initialContext).also {
      log.info("Finished CAS1 calculating eligibility for CRN: ${data.crn}")
      logServiceResult(it)
    }
  }

  private fun logServiceResult(result: ServiceResult) {
    log.info(
      "Service Result: serviceStatus={}, suitableApplicationId={}, action={}, link={}",
      result.serviceStatus,
      result.suitableApplicationId,
      result.action,
      result.link,
    )
  }

  fun calculateEligibilityForCas3(data: DomainData): ServiceResult {
    log.info("Calculating CAS3 eligibility for CRN: ${data.crn}")

    data.cas3Application?.let {
      log.info(
        "CAS3 Data received: id={}, applicationStatus={}, bookingStatus={}",
        data.cas3Application.id,
        data.cas3Application.applicationStatus,
        data.cas3Application.bookingStatus,
      )
    } ?: log.info("CAS3 Data received: No CAS3 application")

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
        .ruleSet("Cas3Validation", cas3ValidationRuleSet, commonContextUpdater)
        .onPass(completion)
        .onFail(notEligible)
        .build()

    val initialContext =
      EvaluationContext(
        data = data,
        currentResult =
        ServiceResult(
          serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
          suitableApplicationId = data.cas3Application?.id,
          link = EligibilityKeys.VIEW_REFERRAL,
        ),
      )

    return tree.eval(initialContext).also {
      log.info("Finished CAS3 calculating eligibility for CRN: ${data.crn}")
      logServiceResult(it)
    }
  }

  fun getDomainData(crn: String): DomainData {
    val eligibilityOrchestrationDto = eligibilityOrchestrationService.getData(crn)
    val currentAccommodation = eligibilityOrchestrationDto.data.cprAddresses?.addresses?.let {
      accommodationQueryService.getCurrentAccommodation(
        crn,
        addresses = it,
      )
    }

    if (eligibilityOrchestrationDto.upstreamFailures.isNotEmpty()) {
      log.error("Eligibility upstream failures for CRN {}: {}", crn, eligibilityOrchestrationDto.upstreamFailures)
    }

    return DomainData(
      crn = crn,
      cpr = eligibilityOrchestrationDto.data.cpr,
      tier = eligibilityOrchestrationDto.data.tier,
      cas1Application = eligibilityOrchestrationDto.data.cas1Application,
      cas3Application = eligibilityOrchestrationDto.data.cas3Application,
      currentAccommodationSummary = currentAccommodation,
    )
  }
}
