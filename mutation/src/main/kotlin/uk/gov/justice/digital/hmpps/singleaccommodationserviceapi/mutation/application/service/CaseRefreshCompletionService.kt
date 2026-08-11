package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_1_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_1_CURRENT_PREMISES
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_3_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_3_CURRENT_PREMISES
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_PRISONER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_TIER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
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
  private val caseSnapshotAssembler: CaseSnapshotAssembler,
  private val caseMapper: CaseMapper,
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
      val aggregate = caseMapper.toAggregate(entity = caseEntity)
      val snapshot = caseSnapshotAssembler.upsertCase(aggregate, projection).snapshot()
      caseRepository.save(caseMapper.merge(caseEntity, snapshot))

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
  private val tierClient: TierClient,
  private val corePersonRecordClient: CorePersonRecordClient,
  private val approvedPremisesClient: ApprovedPremisesClient,
  private val prisonerSearchClient: PrisonerSearchClient,
) {

  fun getCurrentCaseResult(crn: String, prisonNumber: String? = null): OrchestrationResultDto<CaseMutationOrchestrationDto> = orchestrateCase(
    crn = crn,
    loadTier = { tierClient.getTier(crn) },
    loadPersonRecord = { corePersonRecordClient.getByCrn(crn) },
    loadCas1CurrentPremises = { approvedPremisesClient.getCas1CurrentPremises(crn) },
    loadCas3CurrentPremises = { approvedPremisesClient.getCas3CurrentPremises(crn) },
    loadCas1Application = { approvedPremisesClient.getSuitableCas1ApplicationInternal(crn) },
    loadCas3Application = { approvedPremisesClient.getSuitableCas3ApplicationInternal(crn) },
    loadPrisoner = prisonNumber?.let { num -> { prisonerSearchClient.getPrisoner(num) } },
  )

  private fun orchestrateCase(
    crn: String,
    loadTier: () -> Tier,
    loadPersonRecord: () -> CorePersonRecord,
    loadCas1CurrentPremises: () -> Cas1PremisesSummary,
    loadCas3CurrentPremises: () -> Cas3PremisesSummary,
    loadCas1Application: () -> Cas1Application,
    loadCas3Application: () -> Cas3Application,
    loadPrisoner: (() -> Prisoner)? = null,
  ): OrchestrationResultDto<CaseMutationOrchestrationDto> {
    val calls = buildMap {
      put(GET_TIER, loadTier)
      put(GET_CORE_PERSON_RECORD_BY_CRN, loadPersonRecord)
      put(GET_CAS_1_CURRENT_PREMISES, loadCas1CurrentPremises)
      put(GET_CAS_3_CURRENT_PREMISES, loadCas3CurrentPremises)
      put(GET_CAS_1_APPLICATION, loadCas1Application)
      put(GET_CAS_3_APPLICATION, loadCas3Application)
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
        cas1CurrentPremises = results.getResult<Cas1PremisesSummary>(GET_CAS_1_CURRENT_PREMISES),
        cas3CurrentPremises = results.getResult<Cas3PremisesSummary>(GET_CAS_3_CURRENT_PREMISES),
        cas1Application = results.getResult<Cas1Application>(GET_CAS_1_APPLICATION),
        cas3Application = results.getResult<Cas3Application>(GET_CAS_3_APPLICATION),
        prisoner = results.getResult<Prisoner>(GET_PRISONER),
      ),
      upstreamFailures = results.getFailures(),
    )
  }
}
