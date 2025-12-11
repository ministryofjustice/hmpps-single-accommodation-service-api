# 3. API Aggregation Pattern for Reads

Date: 2025-12-11

## Status

Proposed

## Context

The Single Accommodation Service API aggregates data from multiple upstream services:

- **Probation Integration Delius**: Case summaries and case information
- **Probation Integration OASys**: RoSH (Risk of Serious Harm) details
- **Tier Service**: Tier classification information
- **Core Person Record**: Core person record data
- **Approved Premises API**: Approved premises information
- **OASys API** Offender Assessment System
- **Core Person Record** Core Person Record

To build a complete case view, we need to:
1. Call multiple upstream APIs
2. Combine the results into a unified response
3. Handle failures gracefully
4. Optimize for performance (parallel calls, caching)

Sequential API calls would result in:
- High latency (sum of all API call times)
- Poor user experience
- Inefficient resource utilization
- Difficult error handling

This aggregation pattern is fundamental to the read-only operations described in ADR-0001 (DDD Lite), where the aggregator library acts as our "projection layer" for transforming upstream API responses into DTOs.

## Decision

We will use an **API Aggregation Pattern** implemented in the `aggregator` module with the following components:

### 1. AggregatorService with StructuredTaskScope

The `AggregatorService` uses Java's `StructuredTaskScope` for parallel API calls, supporting two types of operations:

- **Standard calls**: Bulk operations that don't require iteration (e.g., fetching case summaries for multiple CRNs)
- **Per-identifier calls**: Operations that need to be executed for each identifier in parallel (e.g., fetching core person record for each CRN)

```kotlin
@Service
class AggregatorService {
    fun orchestrateAsyncCalls(
        standardCallsNoIteration: Map<String, () -> Any> = emptyMap(),
        callsPerIdentifier: CallsPerIdentifier? = null
    ): AggregatorResult {
        // Orchestrate parallel calls using StructuredTaskScope
        // Handles both bulk calls and per-identifier calls in parallel
        // Returns structured results with error handling
    }
}
```

**Key features**:
- Parallel execution of all API calls using `StructuredTaskScope`
- Graceful error handling (failed calls return error messages instead of throwing)
- Support for bulk operations and per-identifier operations
- Nested task scopes for efficient parallelization

### 2. Caching Layer

Caching services wrap upstream API clients to reduce redundant API calls:

```kotlin
@Service
class CorePersonRecordCachingService(
    private val corePersonRecordClient: CorePersonRecordClient,
    private val cacheManager: CacheManager
) {
    fun getCorePersonRecord(crn: String): CorePersonRecord {
        val cache = cacheManager.getCache("corePersonRecord")
        return cache?.get(crn, CorePersonRecord::class.java)
            ?: corePersonRecordClient.getCorePersonRecord(crn)
                .also { cache?.put(crn, it) }
    }
}
```

**Caching strategy**:
- Each upstream service has a dedicated caching service
- Cache keys use identifiers (e.g., CRN) for lookups
- Cache TTLs configured per data type based on volatility

### 3. Orchestration Service Pattern

Orchestration services coordinate aggregation logic using the `AggregatorService`:

```kotlin
@Service
class CaseOrchestrationService(
    val aggregatorService: AggregatorService,
    val probationIntegrationDeliusCachingService: ProbationIntegrationDeliusCachingService,
    val corePersonRecordCachingService: CorePersonRecordCachingService,
    val probationIntegrationOasysCachingService: ProbationIntegrationOasysCachingService,
    val tierCachingService: TierCachingService
) {
    fun getCases(crns: List<String>): List<CaseOrchestrationDto> {
        // Bulk call (no iteration) - single API call for all CRNs
        val bulkCall = mapOf(
            ApiCallKeys.GET_CASE_SUMMARIES to { 
                probationIntegrationDeliusCachingService.getCaseSummaries(crns) 
            }
        )
        
        // Per-identifier calls (parallelized) - one call per CRN, executed in parallel
        val callsPerIdentifier = CallsPerIdentifier(
            identifiersToIterate = crns,
            calls = mapOf(
                ApiCallKeys.GET_CORE_PERSON_RECORD to { crn: String -> 
                    corePersonRecordCachingService.getCorePersonRecord(crn) 
                },
                ApiCallKeys.GET_ROSH_DETAIL to { crn: String -> 
                    probationIntegrationOasysCachingService.getRoshDetails(crn) 
                },
                ApiCallKeys.GET_TIER to { crn: String -> 
                    tierCachingService.getTier(crn) 
                }
            )
        )
        
        // Execute all calls in parallel using AggregatorService
        val results = aggregatorService.orchestrateAsyncCalls(
            standardCallsNoIteration = bulkCall,
            callsPerIdentifier = callsPerIdentifier
        )
        
        // Transform aggregated results into DTOs
        return transformToCaseOrchestrationDto(results, crns)
    }
}
```

**Pattern characteristics**:
- Combines bulk and per-identifier calls efficiently
- All API calls execute in parallel
- Results are aggregated and transformed into DTOs
- Used by query services (read-only operations)

### 4. Error Handling

The `AggregatorService` handles partial failures gracefully:

- Failed API calls return error messages instead of throwing exceptions
- Results contain both successful responses and error messages
- Orchestration services can check for failures and handle appropriately:

```kotlin
fun getCase(crn: String): CaseOrchestrationDto {
    val results = aggregatorService.orchestrateAsyncCalls(...)
    
    // Check for failures and handle appropriately
    val caseSummary = results.standardCallsNoIterationResults
        ?.get(ApiCallKeys.GET_CASE_SUMMARY) as? CaseSummaries
        ?: error("${ApiCallKeys.GET_CASE_SUMMARY} failed for $crn")
    
    val cpr = results.callsPerIdentifierResults
        ?.get(crn)
        ?.get(ApiCallKeys.GET_CORE_PERSON_RECORD) as? CorePersonRecord
        ?: error("${ApiCallKeys.GET_CORE_PERSON_RECORD} failed for $crn")
    
    // ... handle other results
}
```

**Error handling strategy**:
- Individual API call failures don't abort the entire aggregation
- Orchestration services decide how to handle missing data (fail fast vs. partial results)
- Error messages are preserved in results for debugging

## Consequences

### Benefits

- **Reduced latency**: Parallel API calls reduce total response time
- **Better performance**: Caching reduces upstream API load
- **Clear separation**: Aggregation logic is separated from business logic
- **Resilient**: Can handle partial failures gracefully
- **Scalable**: Can handle multiple identifiers efficiently

### Trade-offs

- **Complexity**: More moving parts (aggregator, caching, orchestration)
- **Cache invalidation**: Need to manage cache expiration and invalidation
- **Error handling**: Need to handle partial failures appropriately
- **Resource usage**: Parallel calls consume more resources (threads, connections)

### Implemented Features as of Phase 1

**Caching strategy**: Use appropriate TTL values for different data types __From Redisson__
**Virtual threads**: Use Java virtual threads (enabled in config) for better concurrency

### Upcoming Features Post Phase 1

**Timeout handling**: Set appropriate timeouts for upstream API calls
**Circuit breakers**: Consider circuit breakers for upstream services
**Monitoring**: Track aggregation performance and upstream API health
**Request Coalescing**: Prevent cache miss API waterfall effect __From Redisson__??

## Integration with DDD Lite

This aggregation pattern is the foundation for read operations in our DDD Lite approach (ADR-0001):

- **Read operations**: Use aggregation pattern to fetch and combine data from upstream APIs
- **DTOs**: Aggregated results are transformed into DTOs for presentation
- **No domain entities**: Read operations don't use aggregate roots or domain entities
- **Query services**: Orchestration services are used by query services for read-only operations

When mutations are introduced (Phase 2), aggregates will be hydrated using the same aggregation pattern, but then state changes will be applied through aggregate roots.

## References

- [AggregatorService Implementation](../../../aggregator/src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/aggregator/AggregatorService.kt)
- [CaseOrchestrationService Implementation](../../../src/main/kotlin/uk/gov/justice/digital/hmpps/singleaccommodationserviceapi/case/CaseOrchestrationService.kt)
- [ADR-0001: Adopt DDD Lite for Domain Modeling](./0001-adopt-a-ddd-lite-approach-for-domain-modelling.md)
