package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository

@Service
class CaseRefreshProcessor(
  private val caseRepository: CaseRepository,
  private val caseOrchestrationService: CaseMutationOrchestrationService,
  private val caseRefreshCompletionService: CaseRefreshCompletionService,
  private val caseRefreshRequestService: CaseRefreshRequestService,
  private val failureClassifier: CaseRefreshFailureClassifier,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  fun process(claim: CaseRefreshRequestService.Claim): Result = when (val loadOutcome = loadProjection(claim)) {
    is LoadOutcome.Loaded -> complete(claim, loadOutcome.projection)
    is LoadOutcome.Failed -> recordFailure(claim, loadOutcome.failure)
    LoadOutcome.CaseNotFound -> Result.CaseNotFound
  }

  private fun loadProjection(claim: CaseRefreshRequestService.Claim): LoadOutcome = try {
    val caseEntity = caseRepository.findWithIdentifiersById(claim.caseId)
      ?: return LoadOutcome.CaseNotFound
    val crn = caseEntity.latestCrn()
    val orchestrationResult = caseOrchestrationService.getCurrentCaseResult(
      crn = crn,
      prisonNumber = caseEntity.latestPrisonNumber(),
    )

    if (orchestrationResult.upstreamFailures.isEmpty()) {
      LoadOutcome.Loaded(orchestrationResult.data)
    } else {
      log.warn(
        "Unable to refresh Case projection [caseId={}, crn={}, failedCalls={}]",
        claim.caseId,
        crn,
        orchestrationResult.upstreamFailures.map { it.callKey },
      )
      LoadOutcome.Failed(failureClassifier.classify(orchestrationResult.upstreamFailures))
    }
  } catch (exception: Exception) {
    log.error("Unexpected error loading Case projection [caseId={}]", claim.caseId, exception)
    LoadOutcome.Failed(failureClassifier.unexpected(exception))
  }

  private fun complete(
    claim: CaseRefreshRequestService.Claim,
    projection: CaseMutationOrchestrationDto,
  ): Result = try {
    when (caseRefreshCompletionService.completeRefresh(claim, projection)) {
      CaseRefreshCompletionService.Result.Applied -> Result.Refreshed
      CaseRefreshCompletionService.Result.IgnoredStaleClaim -> Result.IgnoredStaleClaim
    }
  } catch (exception: Exception) {
    log.error("Unexpected error completing Case projection refresh [caseId={}]", claim.caseId, exception)
    recordFailure(claim, failureClassifier.unexpected(exception))
  }

  private fun recordFailure(
    claim: CaseRefreshRequestService.Claim,
    failure: CaseRefreshFailure,
  ): Result = when (caseRefreshRequestService.recordFailure(claim, failure)) {
    CaseRefreshRequestService.FailureDisposition.Handled -> Result.Failed
    CaseRefreshRequestService.FailureDisposition.IgnoredStaleClaim -> Result.IgnoredStaleClaim
  }

  sealed interface Result {
    data object Refreshed : Result
    data object Failed : Result
    data object IgnoredStaleClaim : Result
    data object CaseNotFound : Result
  }

  private sealed interface LoadOutcome {
    data class Loaded(val projection: CaseMutationOrchestrationDto) : LoadOutcome
    data class Failed(val failure: CaseRefreshFailure) : LoadOutcome
    data object CaseNotFound : LoadOutcome
  }
}
