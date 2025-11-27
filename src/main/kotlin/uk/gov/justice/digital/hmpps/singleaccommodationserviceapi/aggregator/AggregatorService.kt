package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator

import org.springframework.stereotype.Service
import java.util.concurrent.StructuredTaskScope

@Service
class AggregatorService {

  fun orchestrateAsyncCalls(
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

  fun orchestrateAsyncCalls(functionCalls: Map<String, () -> Any>): Map<String, Any> = StructuredTaskScope<Any>().use { scope ->
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
