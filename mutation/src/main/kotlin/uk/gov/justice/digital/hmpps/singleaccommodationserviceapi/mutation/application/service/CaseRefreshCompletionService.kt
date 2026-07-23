package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.CallsPerIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseProjectionMapper
import kotlin.collections.forEach

@Service
class CaseRefreshCompletionService(
  private val caseRepository: CaseRepository,
  private val caseRefreshRequestRepository: CaseRefreshRequestRepository,
) {

  @Transactional
  fun completeRefresh(
    claim: CaseRefreshRequestService.Claim,
    projection: CaseMutationOrchestrationDto,
  ) {
    val request = caseRefreshRequestRepository.findByCaseIdForUpdate(claim.caseId) ?: return
    if (request.processingGeneration != claim.generation) {
      return
    }

    val caseEntity = caseRepository.findById(claim.caseId)
      .orElseThrow { IllegalStateException("Case not found while completing refresh [caseId=${claim.caseId}]") }
    caseRepository.save(CaseProjectionMapper.merge(caseEntity, projection))

    if (request.generation == claim.generation) {
      caseRefreshRequestRepository.delete(request)
    } else {
      request.returnToPending()
    }
  }
}

@Service
class CaseProjectionRefreshService(
  private val caseRepository: CaseRepository,
  private val caseOrchestrationService: CaseMutationOrchestrationService,
  private val caseRefreshCompletionService: CaseRefreshCompletionService,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  fun refresh(claim: CaseRefreshRequestService.Claim): Result {
    val caseId = claim.caseId
    val caseEntity = caseRepository.findWithIdentifiersById(caseId) ?: return Result.CaseNotFound
    val crn = caseEntity.latestCrn()
    val orchestrationResult = caseOrchestrationService.getCurrentCaseResult(crn)

    if (orchestrationResult.upstreamFailures.isNotEmpty()) {
      log.warn(
        "Unable to refresh Case projection [caseId={}, crn={}, failedCalls={}]",
        caseId,
        crn,
        orchestrationResult.upstreamFailures.map { it.callKey },
      )
      return Result.UpstreamFailed
    }

    caseRefreshCompletionService.completeRefresh(claim, orchestrationResult.data)
    return Result.Refreshed
  }

  sealed interface Result {
    data object Refreshed : Result
    data object CaseNotFound : Result
    data object UpstreamFailed : Result
  }
}

@Service
class CaseMutationOrchestrationService(
  val aggregatorService: AggregatorService,
  val tierCachingService: TierCachingService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val approvedPremisesCachingService: ApprovedPremisesCachingService,
  private val tierClient: TierClient,
  private val approvedPremisesClient: ApprovedPremisesClient,
) {

  private val log = LoggerFactory.getLogger(javaClass)

  fun getCase(crn: String): CaseMutationOrchestrationDto = orchestrateCase(
    crn = crn,
    loadTier = { tierCachingService.getTier(crn) },
    loadCas1Application = { approvedPremisesCachingService.getSuitableCas1Application(crn) },
  ).data

  fun getCurrentCaseResult(crn: String): OrchestrationResultDto<CaseMutationOrchestrationDto> = orchestrateCase(
    crn = crn,
    loadTier = { tierClient.getTier(crn) },
    loadCas1Application = { approvedPremisesClient.getSuitableCas1ApplicationInternal(crn) },
  )

  private fun orchestrateCase(
    crn: String,
    loadTier: () -> Tier,
    loadCas1Application: () -> Cas1Application,
  ): OrchestrationResultDto<CaseMutationOrchestrationDto> {
    val calls = mapOf(
      ApiCallKeys.GET_TIER to loadTier,
      ApiCallKeys.GET_CAS_1_APPLICATION to loadCas1Application,
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    ).standardCallsNoIterationResults!!
    val tier = results.getResult<Tier>(ApiCallKeys.GET_TIER)
    val cas1Application = results.getResult<Cas1Application>(ApiCallKeys.GET_CAS_1_APPLICATION)
    return OrchestrationResultDto(
      data = CaseMutationOrchestrationDto(crn, cpr = null, tier, cas1Application),
      upstreamFailures = results.getFailures(),
    )
  }

  fun getCases(crns: List<String>): OrchestrationResultDto<List<CaseMutationOrchestrationDto>> {
    val callsPerIdentifier = mapOf(
      ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN to { crn: String -> corePersonRecordCachingService.getCorePersonRecordByCrn(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = emptyMap(),
      callsPerIdentifier = CallsPerIdentifier(
        identifiersToIterate = crns,
        calls = callsPerIdentifier,
      ),
    )
    val cases = results.callsPerIdentifierResults!!.map { (crn, calls) ->
      val tier = calls.getResult<Tier>(ApiCallKeys.GET_TIER)
      val cas1Application = calls.getResult<Cas1Application>(ApiCallKeys.GET_CAS_1_APPLICATION)
      val cpr = calls.getResult<CorePersonRecord>(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN)

      CaseMutationOrchestrationDto(
        crn,
        cpr,
        tier,
        cas1Application,
      )
    }
    val upstreamFailures = results.callsPerIdentifierResults!!.values.flatMap { it.getFailures() }

    logCprFailures(upstreamFailures)
    return OrchestrationResultDto(data = cases, upstreamFailures = upstreamFailures)
  }

  private fun logCprFailures(upstreamFailures: List<UpstreamFailure>) = upstreamFailures.forEach { failure ->
    if (failure.callKey == ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN) {
      log.warn(
        "Unable to get CPR data for provided CRN {}. This will appear in the case-list but a SAS CaseEntity may not be created¬",
        failure.identifier,
      )
    }
  }
}
