package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_PRISONER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_TIER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.PrisonerSearchCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper

@ConditionalOnProperty(
  name = ["case-refresh.enabled"],
  havingValue = "true",
)
@Service
class CaseRefreshCompletionService(
  private val caseRepository: CaseRepository,
  private val caseRefreshRequestRepository: CaseRefreshRequestRepository,
  private val caseRefreshRequestService: CaseRefreshRequestService,
  private val lifecycle: CaseRefreshRequestLifecycleService,
) {

  @Transactional
  fun completeRefresh(
    claim: CaseRefreshRequestService.Claim,
    projection: CaseMutationOrchestrationDto,
  ): Result = when (val request = caseRefreshRequestService.findOwnedRequest(claim)) {
    null -> Result.IGNORED_STALE_CLAIM

    else -> {
      val caseEntity = requireNotNull(caseRepository.findByIdOrNull(claim.caseId)) {
        "Case not found while completing refresh [caseId=${claim.caseId}]"
      }
      val snapshot = CaseMapper.toAggregate(entity = caseEntity).upsertCase(projection).snapshot()
      caseRepository.save(CaseMapper.merge(caseEntity, snapshot))

      if (request.generation == claim.generation) {
        caseRefreshRequestRepository.delete(request)
      } else {
        lifecycle.releaseAfterSuccess(request)
      }
      Result.APPLIED
    }
  }

  enum class Result {
    APPLIED,
    IGNORED_STALE_CLAIM,
  }
}

@Service
class CaseMutationOrchestrationService(
  private val aggregatorService: AggregatorService,
  private val tierCachingService: TierCachingService,
  private val corePersonRecordCachingService: CorePersonRecordCachingService,
  private val tierClient: TierClient,
  private val corePersonRecordClient: CorePersonRecordClient,
  private val prisonerSearchClient: PrisonerSearchClient,
  private val prisonerSearchCachingService: PrisonerSearchCachingService,
) {

  fun getCase(crn: String, prisonNumber: String? = null): CaseMutationOrchestrationDto = orchestrateCase(
    crn = crn,
    loadTier = { tierCachingService.getTier(crn) },
    loadPersonRecord = { corePersonRecordCachingService.getCorePersonRecordByCrn(crn) },
    loadPrisoner = prisonNumber?.let { num -> { prisonerSearchCachingService.getPrisoner(num) } },
  ).data

  fun getCurrentCaseResult(crn: String, prisonNumber: String? = null): OrchestrationResultDto<CaseMutationOrchestrationDto> = orchestrateCase(
    crn = crn,
    loadTier = { tierClient.getTier(crn) },
    loadPersonRecord = { corePersonRecordClient.getByCrn(crn) },
    loadPrisoner = prisonNumber?.let { num -> { prisonerSearchClient.getPrisoner(num) } },
  )

  private fun orchestrateCase(
    crn: String,
    loadTier: () -> Tier,
    loadPersonRecord: () -> CorePersonRecord,
    loadPrisoner: (() -> Prisoner)? = null,
  ): OrchestrationResultDto<CaseMutationOrchestrationDto> {
    val calls = buildMap {
      put(GET_TIER, loadTier)
      put(GET_CORE_PERSON_RECORD_BY_CRN, loadPersonRecord)
      loadPrisoner?.let { put(GET_PRISONER, it) }
    }

    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    ).standardCallsNoIterationResults!!

    return OrchestrationResultDto(
      data = CaseMutationOrchestrationDto(
        crn = crn,
        cpr = results.getResult<CorePersonRecord>(GET_CORE_PERSON_RECORD_BY_CRN),
        tier = results.getResult<Tier>(GET_TIER),
        prisoner = results.getResult<Prisoner>(GET_PRISONER),
      ),
      upstreamFailures = results.getFailures(),
    )
  }
}
