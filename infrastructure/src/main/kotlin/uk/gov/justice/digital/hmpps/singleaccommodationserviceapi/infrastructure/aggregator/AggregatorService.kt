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
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientResponseException
import java.net.http.HttpTimeoutException
import java.time.LocalDateTime

data class CallsPerIdentifier(
  val calls: Map<String, (String) -> Any>,
  val identifiersToIterate: List<String>,
)

data class AggregatorResult(
  val standardCallsNoIterationResults: Map<String, Any>?,
  val callsPerIdentifierResults: Map<String, Map<String, Any>>?,
)

@Service
class AggregatorService {
  private val maxConcurrency: Int = 100
  private val log = LoggerFactory.getLogger(AggregatorService::class.java)

  fun orchestrateAsyncCalls(
    standardCallsNoIteration: Map<String, () -> Any> = emptyMap(),
    callsPerIdentifier: CallsPerIdentifier? = null,
  ): AggregatorResult = runBlocking {
    if (standardCallsNoIteration.isEmpty() && callsPerIdentifier == null) {
      log.info("No API calls to execute")
      return@runBlocking AggregatorResult(emptyMap(), emptyMap())
    }

    log.info("Starting async calls: ${LocalDateTime.now()}")

    val standardCallsSuspend: Map<String, suspend () -> Any> =
      standardCallsNoIteration.mapValues { (_, f) -> suspend { f() } }

    val perIdentifierCallsSuspend: Map<String, suspend (String) -> Any>? =
      callsPerIdentifier?.calls?.mapValues { (_, f) -> f.asSuspend() }

    val semaphore = Semaphore(maxConcurrency)

    val standardDeferred = async(Dispatchers.IO) {
      if (standardCallsSuspend.isNotEmpty()) {
        orchestrateAsyncCalls(standardCallsSuspend, semaphore)
      } else {
        null
      }
    }

    val perIdentifierDeferred = async(Dispatchers.IO) {
      if (perIdentifierCallsSuspend != null) {
        orchestrateAsyncCalls(
          identifiers = callsPerIdentifier.identifiersToIterate,
          functionCalls = perIdentifierCallsSuspend,
          semaphore = semaphore,
        )
      } else {
        null
      }
    }

    val result = AggregatorResult(
      standardCallsNoIterationResults = standardDeferred.await(),
      callsPerIdentifierResults = perIdentifierDeferred.await(),
    )
    log.info("Finished async calls at: ${LocalDateTime.now()}. ")

    result
  }

  private fun <T, R> ((T) -> R).asSuspend(): suspend (T) -> R = { t: T -> this(t) }

  private suspend fun orchestrateAsyncCalls(
    functionCalls: Map<String, suspend () -> Any>,
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

  private suspend fun orchestrateAsyncCalls(
    identifiers: List<String>,
    functionCalls: Map<String, suspend (String) -> Any>,
    semaphore: Semaphore,
  ): Map<String, Map<String, Any>> = coroutineScope {
    identifiers.associateWith { identifier ->
      async(Dispatchers.IO) {
        functionCalls.mapValues { (key, supplier) ->
          async(Dispatchers.IO) {
            semaphore.withPermit {
              try {
                AggregatorCallOutcome.Success(supplier(identifier))
              } catch (e: Exception) {
                classifyAndWrap("$key[$identifier]", e)
              }
            }
          }
        }.mapValues { (_, deferred) -> deferred.await() }
      }
    }.mapValues { (_, deferred) -> deferred.await() }
  }

  private fun classifyAndWrap(key: String, exception: Exception): AggregatorCallOutcome.Failure = when (exception) {
    is RestClientResponseException -> {
      log.error("Upstream HTTP error for call '{}': {} {}", key, exception.statusCode, exception.message)
      AggregatorCallOutcome.Failure(
        FailureType.UPSTREAM_HTTP_ERROR,
        ErrorDetail(
          httpStatus = HttpStatus.resolve(exception.statusCode.value()),
          message = exception.message ?: "Upstream HTTP error",
        ),
      )
    }
    is ResourceAccessException -> {
      val isTimeout = exception.cause is HttpTimeoutException
      if (isTimeout) {
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
}
