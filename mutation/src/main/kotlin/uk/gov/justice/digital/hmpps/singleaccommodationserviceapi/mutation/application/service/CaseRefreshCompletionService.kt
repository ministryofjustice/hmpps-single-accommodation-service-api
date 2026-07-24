package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PremisesSummary
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseProjectionMapper

@Service
class CaseRefreshCompletionService(
  private val caseRepository: CaseRepository,
  private val caseRefreshRequestRepository: CaseRefreshRequestRepository,
) {

  @Transactional
  fun completeRefresh(
    claim: CaseRefreshRequestService.Claim,
    projection: CaseMutationOrchestrationDto,
  ): Result {
    val request = caseRefreshRequestRepository.findByCaseIdForUpdate(claim.caseId)
      ?: return Result.IgnoredStaleClaim
    if (!request.isOwnedBy(claim.generation, claim.claimId)) {
      return Result.IgnoredStaleClaim
    }

    val caseEntity = caseRepository.findById(claim.caseId)
      .orElseThrow { IllegalStateException("Case not found while completing refresh [caseId=${claim.caseId}]") }
    caseRepository.save(CaseProjectionMapper.merge(caseEntity, projection))
    if (request.generation == claim.generation) {
      caseRefreshRequestRepository.delete(request)
    } else {
      request.releaseForNewerGeneration()
    }
    return Result.Applied
  }

  enum class Result {
    Applied,
    IgnoredStaleClaim,
  }
}

@Service
class CaseMutationOrchestrationService(
  private val aggregatorService: AggregatorService,
  private val tierCachingService: TierCachingService,
  private val corePersonRecordCachingService: CorePersonRecordCachingService,
  private val approvedPremisesCachingService: ApprovedPremisesCachingService,
  private val tierClient: TierClient,
  private val approvedPremisesClient: ApprovedPremisesClient,
  private val corePersonRecordClient: CorePersonRecordClient,
  private val prisonerSearchClient: PrisonerSearchClient,
  private val prisonerSearchCachingService: PrisonerSearchCachingService,
) {

  fun getCase(crn: String, prisonNumber: String? = null): CaseMutationOrchestrationDto = orchestrateCase(
    crn = crn,
    loadTier = { tierCachingService.getTier(crn) },
    loadCas1Application = { approvedPremisesCachingService.getSuitableCas1Application(crn) },
    loadCas3Application = { approvedPremisesCachingService.getSuitableCas3Application(crn) },
    loadCas1CurrentPremises = { approvedPremisesCachingService.getCas1CurrentPremises(crn) },
    loadCas3CurrentPremises = { approvedPremisesCachingService.getCas3CurrentPremises(crn) },
    loadPersonRecord = { corePersonRecordCachingService.getCorePersonRecordByCrn(crn) },
    loadPrisoner = prisonNumber?.let { num -> { prisonerSearchCachingService.getPrisoner(num) } },
  ).data

  fun getCurrentCaseResult(crn: String, prisonNumber: String? = null): OrchestrationResultDto<CaseMutationOrchestrationDto> = orchestrateCase(
    crn = crn,
    loadTier = { tierClient.getTier(crn) },
    loadCas1Application = { approvedPremisesClient.getSuitableCas1ApplicationInternal(crn) },
    loadCas3Application = { approvedPremisesClient.getSuitableCas3ApplicationInternal(crn) },
    loadCas1CurrentPremises = { approvedPremisesClient.getCas1CurrentPremises(crn) },
    loadCas3CurrentPremises = { approvedPremisesClient.getCas3CurrentPremises(crn) },
    loadPersonRecord = { corePersonRecordClient.getByCrn(crn) },
    loadPrisoner = prisonNumber?.let { num -> { prisonerSearchClient.getPrisoner(num) } },
  )

  private fun orchestrateCase(
    crn: String,
    loadTier: () -> Tier,
    loadCas1Application: () -> Cas1Application,
    loadCas3Application: () -> Cas3Application,
    loadCas1CurrentPremises: () -> Cas1PremisesSummary,
    loadCas3CurrentPremises: () -> Cas3PremisesSummary,
    loadPersonRecord: () -> CorePersonRecord,
    loadPrisoner: (() -> Prisoner)? = null,
  ): OrchestrationResultDto<CaseMutationOrchestrationDto> {
    val calls = buildMap<String, () -> Any> {
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
        cas1Application = results.getResult<Cas1Application>(GET_CAS_1_APPLICATION),
        cas3Application = results.getResult<Cas3Application>(GET_CAS_3_APPLICATION),
        cas1CurrentPremises = results.getResult<Cas1PremisesSummary>(GET_CAS_1_CURRENT_PREMISES),
        cas3CurrentPremises = results.getResult<Cas3PremisesSummary>(GET_CAS_3_CURRENT_PREMISES),
        prisoner = results.getResult<Prisoner>(GET_PRISONER),
      ),
      upstreamFailures = results.getFailures(),
    )
  }
}
