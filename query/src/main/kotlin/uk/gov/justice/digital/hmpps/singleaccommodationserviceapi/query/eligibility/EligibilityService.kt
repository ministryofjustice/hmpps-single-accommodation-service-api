package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.completion.DtrCompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.completion.DtrCompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.eligibility.DtrEligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.suitability.DtrSuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.suitability.DtrSuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.DtrUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.DtrUpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine

@Service
class EligibilityService(
  private val cas1CompletionRuleSet: Cas1CompletionRuleSet,
  private val cas1ContextUpdater: Cas1ContextUpdater,
  private val cas1EligibilityRuleSet: Cas1EligibilityRuleSet,
  private val cas1SuitabilityRuleSet: Cas1SuitabilityRuleSet,
  private val cas1ValidationRuleSet: Cas1ValidationRuleSet,
  private val cas3CompletionRuleSet: Cas3CompletionRuleSet,
  private val cas3ContextUpdater: Cas3ContextUpdater,
  private val cas3EligibilityRuleSet: Cas3EligibilityRuleSet,
  private val cas3SuitabilityRuleSet: Cas3SuitabilityRuleSet,
  private val cas3ValidationRuleSet: Cas3ValidationRuleSet,
  private val commonContextUpdater: CommonContextUpdater,
  private val dtrCompletionRuleSet: DtrCompletionRuleSet,
  private val dtrCompletionContextUpdater: DtrCompletionContextUpdater,
  private val dtrSuitabilityContextUpdater: DtrSuitabilityContextUpdater,
  private val dtrEligibilityRuleSet: DtrEligibilityRuleSet,
  private val dtrSuitabilityRuleSet: DtrSuitabilityRuleSet,
  private val dtrUpcomingRuleSet: DtrUpcomingRuleSet,
  private val dtrUpcomingContextUpdater: DtrUpcomingContextUpdater,
  private val dutyToReferQueryService: DutyToReferQueryService,
  private val eligibilityOrchestrationService: EligibilityOrchestrationService,
  @Qualifier("defaultRulesEngine")
  private val engine: RulesEngine,

) {
  private val treeBuilder = DecisionTreeBuilder(engine)
  private val log = LoggerFactory.getLogger(this::class.java)

  fun getEligibility(personDto: PersonDto, caseEntity: CaseEntity?, dutyToRefer: DutyToReferDto?): EligibilityDto {
    log.info("Calculating eligibility for CRN: ${personDto.crn} from the sas_case table")
    val data = DomainData(
      personDto,
      caseEntity,
      dutyToRefer,
    )
    return getEligibility(data)
  }

  fun getEligibility(crn: String): EligibilityDto {
    log.info("Calculating eligibility for CRN: $crn using external APIs")
    val data = getDomainData(crn)
    return getEligibility(data)
  }

  fun getEligibility(data: DomainData): EligibilityDto {
    log.info(
      "External data received: crn={}, releaseDate={}, tierScore={}, sex={}, crsStatus={}, currentAccommodationArrangementType={}, currentAccommodationEndDate={}, hasNextAccommodation={}",
      data.crn,
      data.releaseDate,
      data.tierScore,
      data.sex,
      data.crsStatus,
      data.currentAccommodationArrangementType,
      data.currentAccommodationEndDate,
      data.hasNextAccommodation,
    )

    val cas1 = calculateEligibilityForCas1(data)
    val cas3 = calculateEligibilityForCas3(data)
    val dtr = calculateEligibilityForDtr(data)

    return EligibilityTransformer.toEligibilityDto(
      crn = data.crn,
      cas1 = cas1,
      cas3 = cas3,
      dtr = dtr,
      dutyToReferData = data.dutyToReferData,
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

  fun calculateEligibilityForDtr(data: DomainData): ServiceResult {
    log.info("Calculating Dtr eligibility for CRN: ${data.crn}")

    log.info(
      "DTR Data received: dtrStatus={}, dtrSubmissionDate={}",
      data.dtrStatus,
      data.dtrSubmissionDate,
    )

    val confirmed = treeBuilder.confirmed()
    val notEligible = treeBuilder.notEligible()
    val accepted = treeBuilder.accepted()

    val eligibility =
      treeBuilder
        .ruleSet("DtrEligibility", dtrEligibilityRuleSet, commonContextUpdater)
        .onPass(confirmed)
        .onFail(notEligible)
        .build()

    val completion =
      treeBuilder
        .ruleSet("DtrCompletion", dtrCompletionRuleSet, dtrCompletionContextUpdater)
        .onPass(accepted)
        .onFail(confirmed)
        .build()

    val suitability =
      treeBuilder
        .ruleSet("DtrSuitability", dtrSuitabilityRuleSet, dtrSuitabilityContextUpdater)
        .onPass(completion)
        .onFail(eligibility)
        .build()

    val tree =
      treeBuilder
        .ruleSet("DtrUpcoming", dtrUpcomingRuleSet, dtrUpcomingContextUpdater)
        .onPass(suitability)
        .onFail(eligibility)
        .build()

    val initialContext =
      EvaluationContext(
        data = data,
        currentResult =
        ServiceResult(
          serviceStatus = ServiceStatus.ACCEPTED,
        ),
      )

    return tree.eval(initialContext).also {
      log.info("Finished DTR calculating eligibility for CRN: ${data.crn}")
      logServiceResult(it)
    }
  }

  fun getDomainData(crn: String): DomainData {
    val dutyToRefer = dutyToReferQueryService.getPotentialDutyToRefer(crn)

    val eligibilityOrchestrationDto = eligibilityOrchestrationService.getData(crn)

    val prisonerNumbers = eligibilityOrchestrationDto.cpr.identifiers?.prisonNumbers

    val prisonerData = prisonerNumbers?.let { eligibilityOrchestrationService.getPrisonerData(prisonerNumbers) }

    return DomainData(
      crn = crn,
      cpr = eligibilityOrchestrationDto.cpr,
      tier = eligibilityOrchestrationDto.tier,
      prisonerData = prisonerData,
      cas1Application = eligibilityOrchestrationDto.cas1Application,
      cas3Application = eligibilityOrchestrationDto.cas3Application,
      dutyToRefer = dutyToRefer,
      currentAccommodation = null,
      nextAccommodation = null,
    )
  }
}
