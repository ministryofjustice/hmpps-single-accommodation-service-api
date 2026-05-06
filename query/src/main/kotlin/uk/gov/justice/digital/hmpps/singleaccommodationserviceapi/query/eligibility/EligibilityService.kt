package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion.Cas1CompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion.Cas1CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.Cas1EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1SuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability.Cas1SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.Cas1UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.Cas1UpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.validation.Cas1ValidationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion.Cas3CompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion.Cas3CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.Cas3EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3SuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.validation.Cas3ValidationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.CommonContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion.CrsCompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility.CrsEligibilityRuleSet
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
  private val accommodationQueryService: AccommodationQueryService,
  private val caseRepository: CaseRepository,
  private val dutyToReferQueryService: DutyToReferQueryService,
  private val eligibilityOrchestrationService: EligibilityOrchestrationService,
  @Qualifier("defaultRulesEngine")
  private val engine: RulesEngine,

  // COMMON
  private val commonContextUpdater: CommonContextUpdater,

  // CAS1
  private val cas1CompletionRuleSet: Cas1CompletionRuleSet,
  private val cas1CompletionContextUpdater: Cas1CompletionContextUpdater,
  private val cas1EligibilityRuleSet: Cas1EligibilityRuleSet,
  private val cas1SuitabilityRuleSet: Cas1SuitabilityRuleSet,
  private val cas1UpcomingRuleSet: Cas1UpcomingRuleSet,
  private val cas1UpcomingContextUpdater: Cas1UpcomingContextUpdater,
  private val cas1ValidationRuleSet: Cas1ValidationRuleSet,
  private val cas1SuitabilityContextUpdater: Cas1SuitabilityContextUpdater,

  // CAS3
  private val cas3CompletionRuleSet: Cas3CompletionRuleSet,
  private val cas3CompletionContextUpdater: Cas3CompletionContextUpdater,
  private val cas3EligibilityRuleSet: Cas3EligibilityRuleSet,
  private val cas3SuitabilityRuleSet: Cas3SuitabilityRuleSet,
  private val cas3SuitabilityContextUpdater: Cas3SuitabilityContextUpdater,
  private val cas3UpcomingRuleSet: Cas3UpcomingRuleSet,
  private val cas3UpcomingContextUpdater: Cas3UpcomingContextUpdater,
  private val cas3ValidationRuleSet: Cas3ValidationRuleSet,

  // DTR
  private val dtrCompletionRuleSet: DtrCompletionRuleSet,
  private val dtrCompletionContextUpdater: DtrCompletionContextUpdater,
  private val dtrSuitabilityContextUpdater: DtrSuitabilityContextUpdater,
  private val dtrEligibilityRuleSet: DtrEligibilityRuleSet,
  private val dtrSuitabilityRuleSet: DtrSuitabilityRuleSet,
  private val dtrUpcomingRuleSet: DtrUpcomingRuleSet,
  private val dtrUpcomingContextUpdater: DtrUpcomingContextUpdater,

  // CRS
  private val crsEligibilityRuleSet: CrsEligibilityRuleSet,
  private val crsCompletionRuleSet: CrsCompletionRuleSet,
  private val crsContextUpdater: CrsContextUpdater,
) {
  private val treeBuilder = DecisionTreeBuilder(engine)
  private val log = LoggerFactory.getLogger(this::class.java)

  fun getEligibility(
    crn: String,
    gender: String,
    caseEntity: CaseEntity?,
    dutyToRefer: DutyToReferDto?,
  ): EligibilityDto {
    log.info("Calculating eligibility for CRN: $crn from the sas_case table")
    val data = DomainData(
      crn = crn,
      sexCode = SexCode.findByGender(gender),
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
    log.debug(
      "Eligibility input data: crn={}, currentAccommodation.?endDate={}, tierScore={}, sex={}, currentAccommodationIsPrisonCas1Cas2orCas2v2={}, currentAccommodationIsPrivate={}, hasNextAccommodation={}",
      data.crn,
      data.currentAccommodation?.endDate,
      data.tierScore,
      data.sex,
      data.currentAccommodation?.isPrisonCas1Cas2OrCas2v2,
      data.currentAccommodation?.isPrivate,
      data.hasNextAccommodation,
    )

    val cas1 = calculateEligibilityForCas1(data)
    val cas3 = calculateEligibilityForCas3(data)
    val crs = calculateEligibilityForCrs(data)
    val dtr = calculateEligibilityForDtr(data)

    return EligibilityTransformer.toEligibilityDto(
      crn = data.crn,
      cas1 = cas1,
      cas3 = cas3,
      dtr = dtr,
      crs = crs,
      data = data,
    ).also { log.info("Finished calculating eligibility for CRN: ${data.crn}") }
  }

  fun calculateEligibilityForCas1(data: DomainData): ServiceResult {
    log.info("Calculating CAS1 eligibility for CRN: ${data.crn}")

    data.cas1Application?.let {
      log.debug(
        "CAS1 Data received: id={}, applicationStatus={}, requestForPlacementStatus={}, placementStatus={}",
        it.id,
        it.applicationStatus,
        it.requestForPlacementStatus,
        it.placementStatus,
      )
    } ?: log.debug("CAS1 Data received: No CAS1 application")

    // Build tree declaratively:
    val confirmed = treeBuilder.confirmed()
    val notEligible = treeBuilder.notEligible()
    val placementBooked = treeBuilder.placementBooked()

    val eligibility =
      treeBuilder
        .ruleSet("Cas1Eligibility", cas1EligibilityRuleSet, commonContextUpdater)
        .onPass(confirmed)
        .onFail(notEligible)
        .build()

    val completion =
      treeBuilder
        .ruleSet("Cas1Completion", cas1CompletionRuleSet, cas1CompletionContextUpdater)
        .onPass(placementBooked)
        .onFail(confirmed)
        .build()

    val suitability =
      treeBuilder
        .ruleSet("Cas1Suitability", cas1SuitabilityRuleSet, cas1SuitabilityContextUpdater)
        .onPass(completion)
        .onFail(eligibility)
        .build()

    val upcoming =
      treeBuilder
        .ruleSet("Cas1Upcoming", cas1UpcomingRuleSet, cas1UpcomingContextUpdater)
        .onPass(suitability)
        .onFail(eligibility)
        .build()

    val tree =
      treeBuilder
        .ruleSet("Cas1Validation", cas1ValidationRuleSet, commonContextUpdater)
        .onPass(upcoming)
        .onFail(notEligible)
        .build()

    val initialContext =
      EvaluationContext(
        data = data,
        currentResult =
        ServiceResult(
          serviceStatus = ServiceStatus.PLACEMENT_BOOKED,
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
      "Service Result: serviceStatus={}, action={}, link={}",
      result.serviceStatus,
      result.action,
      result.link,
    )
  }

  fun calculateEligibilityForCrs(data: DomainData): ServiceResult {
    log.info("Calculating Crs eligibility for CRN: ${data.crn}")

    data.commissionedRehabilitativeServices?.let {
      log.debug(
        "CRS Data received: status={}, submissionDate={}",
        it.status,
        it.submissionDate,
      )
    } ?: log.debug("CRS Data received: No CRS application")

    val confirmed = treeBuilder.confirmed()
    val notEligible = treeBuilder.notEligible()

    val eligibility =
      treeBuilder
        .ruleSet("CrsEligibility", crsEligibilityRuleSet, commonContextUpdater)
        .onPass(confirmed)
        .onFail(notEligible)
        .build()

    val tree =
      treeBuilder
        .ruleSet("CrsCompletion", crsCompletionRuleSet, crsContextUpdater)
        .onPass(confirmed)
        .onFail(eligibility)
        .build()

    val initialContext =
      EvaluationContext(
        data = data,
        currentResult =
        ServiceResult(
          serviceStatus = ServiceStatus.SUBMITTED,
          link = EligibilityKeys.VIEW_REFER_AND_MONITOR,
        ),
      )

    return tree.eval(initialContext).also {
      log.info("Finished CRS calculating eligibility for CRN: ${data.crn}")
      logServiceResult(it)
    }
  }

  fun calculateEligibilityForCas3(data: DomainData): ServiceResult {
    log.info("Calculating CAS3 eligibility for CRN: ${data.crn}")

    data.cas3Application?.let {
      log.debug(
        "CAS3 Data received: id={}, applicationStatus={}, bookingStatus={}",
        it.id,
        it.applicationStatus,
        it.bookingStatus,
      )
    } ?: log.debug("CAS3 Data received: No CAS3 application")

    val confirmed = treeBuilder.confirmed()
    val notEligible = treeBuilder.notEligible()
    val bookingConfirmed = treeBuilder.bookingConfirmed()

    val eligibility =
      treeBuilder
        .ruleSet("Cas3Eligibility", cas3EligibilityRuleSet, commonContextUpdater)
        .onPass(confirmed)
        .onFail(notEligible)
        .build()

    val completion =
      treeBuilder
        .ruleSet("Cas3Completion", cas3CompletionRuleSet, cas3CompletionContextUpdater)
        .onPass(bookingConfirmed)
        .onFail(confirmed)
        .build()

    val suitability =
      treeBuilder
        .ruleSet("Cas3Suitability", cas3SuitabilityRuleSet, cas3SuitabilityContextUpdater)
        .onPass(completion)
        .onFail(eligibility)
        .build()

    val upcoming =
      treeBuilder
        .ruleSet("Cas3Upcoming", cas3UpcomingRuleSet, cas3UpcomingContextUpdater)
        .onPass(suitability)
        .onFail(eligibility)
        .build()

    val tree =
      treeBuilder
        .ruleSet("Cas3Validation", cas3ValidationRuleSet, commonContextUpdater)
        .onPass(upcoming)
        .onFail(notEligible)
        .build()

    val initialContext =
      EvaluationContext(
        data = data,
        currentResult =
        ServiceResult(
          serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
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

    data.dutyToRefer?.let {
      log.debug(
        "DTR Data received: status={}, submissionDate={}",
        it.status,
        it.submission?.submissionDate,
      )
    } ?: log.debug("DTR Data received: No DTR application")

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
    val caseEntity = caseRepository.findByCrn(crn)

    val dutyToRefer = caseEntity?.let { dutyToReferQueryService.getDutyToRefer(caseEntity, crn) }

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
      dutyToRefer = dutyToRefer,
      // TODO connect to crs endpoint when it becomes available
      commissionedRehabilitativeServices = null,
    )
  }
}
