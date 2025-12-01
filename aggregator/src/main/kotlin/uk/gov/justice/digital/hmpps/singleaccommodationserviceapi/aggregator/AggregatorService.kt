package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator

import org.springframework.stereotype.Service
import java.util.concurrent.StructuredTaskScope

data class CallsPerIdentifier(
  val calls: Map<String, (String) -> Any>,
  val identifiersToIterate: List<String>,
)

data class AggregatorResult(
  val standardCallsNoIterationResults: Map<String, Any>?,
  val callsPerIdentifierResults: Map<String, Map<String, Any>>?
)

@Service
class AggregatorService {
  fun orchestrateAsyncCalls(
    standardCallsNoIteration: Map<String, () -> Any> = emptyMap(),
    callsPerIdentifier: CallsPerIdentifier? = null
  ): AggregatorResult {
    if (standardCallsNoIteration.isEmpty() && callsPerIdentifier == null) {
      throw IllegalArgumentException("No API calls to execute")
    }

    val asyncCallsToMake = mutableMapOf<String, () -> Map<String, Any>>()
    if (standardCallsNoIteration.isNotEmpty()) {
      asyncCallsToMake["standardCallsNoIteration"] = {
        orchestrateAsyncCalls(
          functionCalls = standardCallsNoIteration
        )
      }
    }
    if (callsPerIdentifier != null) {
      asyncCallsToMake["callsPerIdentifier"] = {
        orchestrateAsyncCalls(
          identifiers = callsPerIdentifier.identifiersToIterate,
          functionCalls = callsPerIdentifier.calls
        )
      }
    }

    val asyncResults: Map<String, Any> = orchestrateAsyncCalls(
      functionCalls = asyncCallsToMake
    )

    return AggregatorResult(
      standardCallsNoIterationResults = if (standardCallsNoIteration.isNotEmpty()) {
        asyncResults["standardCallsNoIteration"] as Map<String, Any>
      } else null,
      callsPerIdentifierResults = if (callsPerIdentifier != null) {
        asyncResults["callsPerIdentifier"] as Map<String, Map<String, Any>>
      } else null
    )
  }

  private fun orchestrateAsyncCalls(
    identifiers: List<String>,
    functionCalls: Map<String, (String) -> Any>,
  ): Map<String, Map<String, Any>> = StructuredTaskScope<Any>().use { outerScope ->
    val outerSubtasks = identifiers.associateWith { identifier ->
      outerScope.fork {
        StructuredTaskScope<Any>().use { innerScope ->
          val innerSubtasks = functionCalls.mapValues { (_, supplier) ->
            innerScope.fork {
              runCatching { supplier(identifier) }
                .getOrElse { "Failed with message: ${it.message}" }
            }
          }
          innerScope.join()
          innerSubtasks.mapValues { (_, innerSubtask) -> innerSubtask.get() }
        }
      }
    }
    outerScope.join()
    outerSubtasks.mapValues { (_, outerSubtask) -> outerSubtask.get() }
  }

  private fun orchestrateAsyncCalls(functionCalls: Map<String, () -> Any>): Map<String, Any> = StructuredTaskScope<Any>().use { scope ->
    val tasks = functionCalls.mapValues { (_, supplier) ->
      scope.fork {
        runCatching { supplier() }
          .getOrElse { "Failed with message: ${it.message}" }
      }
    }

    scope.join()
    tasks.mapValues { (_, subtask) -> subtask.get() }
  }
}
