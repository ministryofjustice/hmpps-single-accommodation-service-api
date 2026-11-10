package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PageMetadata
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.util.isTimeout
import java.time.LocalDateTime

data class AggregatorResult(
  val standardCallsNoIterationResults: Map<String, Any>?,
)

@Service
class AggregatorService(
  private val upstreamFailureReporters: List<UpstreamFailureReporter> = emptyList(),
) {
  private val maxConcurrency: Int = 100
  private val semaphore = Semaphore(maxConcurrency)
  private val log = LoggerFactory.getLogger(AggregatorService::class.java)

  fun orchestrateAsyncCalls(
    standardCallsNoIteration: Map<String, () -> Any?> = emptyMap(),
  ): AggregatorResult = runBlocking {
    if (standardCallsNoIteration.isEmpty()) {
      log.debug("No API calls to execute")
      return@runBlocking AggregatorResult(emptyMap())
    }

    log.debug("Starting async calls: {}", LocalDateTime.now())

    val standardCallsSuspend: Map<String, suspend () -> Any?> =
      standardCallsNoIteration.mapValues { (_, f) -> suspend { f() } }

    val standardDeferred = async(Dispatchers.IO) {
      orchestrateAsyncCalls(standardCallsSuspend, semaphore)
    }

    val result = AggregatorResult(
      standardCallsNoIterationResults = standardDeferred.await(),
    )
    log.debug("Finished async calls at: {}. ", LocalDateTime.now())

    result
  }

  private suspend fun orchestrateAsyncCalls(
    functionCalls: Map<String, suspend () -> Any?>,
    semaphore: Semaphore,
  ): Map<String, Any> = coroutineScope {
    functionCalls.mapValues { (key, call) ->
      async(Dispatchers.IO) {
        semaphore.withPermit {
          try {
            AggregatorCallOutcome.Success(call())
          } catch (e: Exception) {
            classifyAndWrap(key, e)
          }
        }
      }
    }.mapValues { (_, deferred) -> deferred.await() }
  }

  private fun classifyAndWrap(key: String, exception: Exception): AggregatorCallOutcome.Failure {
    val failure = when (exception) {
      is WebClientResponseException -> {
        log.error("Upstream HTTP error for call '{}': {} {}", key, exception.statusCode, exception.message)
        AggregatorCallOutcome.Failure(
          FailureType.UPSTREAM_HTTP_ERROR,
          ErrorDetail(
            httpStatus = HttpStatus.resolve(exception.statusCode.value()),
            message = exception.message,
          ),
        )
      }
      is WebClientRequestException -> {
        if (exception.isTimeout()) {
          log.error("Timeout for call '{}': {}", key, exception.message)
          AggregatorCallOutcome.Failure(
            FailureType.TIMEOUT,
            ErrorDetail(httpStatus = null, message = exception.message ?: "Request timed out"),
          )
        } else {
          log.error("Unknown error for call '{}': {}", key, exception.message, exception)
          AggregatorCallOutcome.Failure(
            FailureType.UNKNOWN_ERROR,
            ErrorDetail(httpStatus = null, message = exception.message ?: "Unknown error"),
          )
        }
      }
      else -> {
        log.error("Unknown error for call '{}': {}", key, exception.message, exception)
        AggregatorCallOutcome.Failure(
          FailureType.UNKNOWN_ERROR,
          ErrorDetail(httpStatus = null, message = exception.message ?: "Unknown error"),
        )
      }
    }
    upstreamFailureReporters.forEach { reporter ->
      runCatching { reporter.report(key, failure, exception) }
        .onFailure { t -> log.warn("UpstreamFailureReporter {} threw while reporting callKey={}", reporter::class.java.name, key, t) }
    }
    return failure
  }
}

inline fun <reified R : Any, T> AggregatorService.fetchAllPages(
  callKeyForPage: (Long) -> String,
  crossinline fetchPage: (Long) -> R,
  pageMetadata: (R) -> PageMetadata,
  content: (R) -> List<T>,
): OrchestrationResultDto<List<T>> {
  val firstPageKey = callKeyForPage(0)
  val firstPageResults = orchestrateAsyncCalls(
    standardCallsNoIteration = mapOf(firstPageKey to { fetchPage(0) }),
  ).standardCallsNoIterationResults!!
  val firstPage = firstPageResults.getResult<R>(firstPageKey)

  val remainingPageNumbers = firstPage?.let { 1 until pageMetadata(it).totalPages } ?: LongRange.EMPTY
  val remainingPageResults = orchestrateAsyncCalls(
    standardCallsNoIteration = remainingPageNumbers.associate { number -> callKeyForPage(number) to { fetchPage(number) } },
  ).standardCallsNoIterationResults!!
  val remainingPages = remainingPageNumbers.mapNotNull { number -> remainingPageResults.getResult<R>(callKeyForPage(number)) }

  return OrchestrationResultDto(
    data = (listOfNotNull(firstPage) + remainingPages).flatMap { content(it) },
    upstreamFailures = firstPageResults.getFailures() + remainingPageResults.getFailures(),
  )
}
