# JPA & Transaction Best Practices: A Refined Guide

## Table of Contents

1. [Purpose](#purpose)
2. [Core Principles](#core-principles)
    - [Explicit Over Implicit](#explicit-over-implicit)
    - [Short Transactions](#short-transactions)
    - [Clear Transaction Boundaries](#clear-transaction-boundaries)
3. [Transaction Boundaries: Service Layer, Not Controllers](#transaction-boundaries-service-layer-not-controllers)
    - [❌ Anti-Pattern: Controller Transactions](#-anti-pattern-controller-transactions)
    - [✅ Best Practice: Service Layer Transactions](#-best-practice-service-layer-transactions)
4. [OSIV Must Be Disabled: Explicit Fetch Plans](#osiv-must-be-disabled-explicit-fetch-plans)
    - [❌ Anti-Pattern: OSIV with Lazy Loading](#-anti-pattern-osiv-with-lazy-loading)
    - [✅ Best Practice: Explicit Fetch Plans](#-best-practice-explicit-fetch-plans)
5. [Unidirectional Aggregates: Avoid Bidirectional Relationships](#unidirectional-aggregates-avoid-bidirectional-relationships)
    - [❌ Anti-Pattern: Bidirectional Relationships](#-anti-pattern-bidirectional-relationships)
    - [✅ Best Practice: Unidirectional Aggregates](#-best-practice-unidirectional-aggregates)
6. [External Calls Outside Transactions](#external-calls-outside-transactions)
    - [❌ Anti-Pattern: External Calls in Transactions](#-anti-pattern-external-calls-in-transactions)
    - [✅ Best Practice: External Calls Before/After Transactions](#-best-practice-external-calls-beforeafter-transactions)
7. [Outbox Pattern: Reliable Event Publishing](#outbox-pattern-reliable-event-publishing)
    - [❌ Anti-Pattern: Publishing Events Directly in Transactions](#-anti-pattern-publishing-events-directly-in-transactions)
    - [✅ Best Practice: Outbox Pattern](#-best-practice-outbox-pattern)
8. [Service Interface Design: Avoid Hidden Database Calls](#service-interface-design-avoid-hidden-database-calls)
    - [❌ Anti-Pattern: Hidden Database Calls in Services](#-anti-pattern-hidden-database-calls-in-services)
    - [✅ Best Practice: Explicit Data Passing](#-best-practice-explicit-data-passing)
9. [Complete Working Example](#complete-working-example)
10. [Key Takeaways](#key-takeaways)
- [Performance Benefits](#performance-benefits)
- [Reliability Benefits](#reliability-benefits)
- [Maintainability Benefits](#maintainability-benefits)
- [Critical Rules](#critical-rules)

---

## Purpose

This document presents the **why** behind JPA and transaction patterns, focusing on the technical reasons that make certain approaches superior. Understanding these principles is crucial for making informed architectural decisions and defending them effectively.

## Core Principles

### Explicit Over Implicit
- **Why**: Predictable performance, no hidden surprises, easier debugging
- **How**: Use explicit fetch plans, clear transaction boundaries, explicit data loading

### Short Transactions
- **Why**: Better resource utilization, reduced lock contention, higher throughput
- **How**: Keep transactions focused on single responsibilities, move external calls outside

### Clear Transaction Boundaries
- **Why**: Better testability, clear dependencies, easier reasoning about performance
- **How**: Transactions in service layer, extract data in controllers, pass as parameters to services

## Transaction Boundaries: Service Layer, Not Controllers

### ❌ Anti-Pattern: Controller Transactions

```kotlin
@RestController
class ApplicationController {
    @Transactional  // ❌ BAD: Long transaction spanning entire request
    @PostMapping("/applications")
    fun createApplication(@RequestBody request: CreateApplicationRequest): ResponseEntity<ApplicationResponse> {
        val user = userService.getUserForRequest()
        
        // External API call inside transaction (1-5 seconds!)
        val validationResult = externalApiClient.validateOffender(request.crn)
        
        // Business logic in controller
        val application = applicationRepository.save(Application(request))
        
        return ResponseEntity.ok(application)
    }
}
```

**Problems:**
- **Long Transaction Duration**: Spans entire HTTP request (parsing, validation, external calls, DB ops, serialization)
- **Connection Pool Exhaustion**: Each request holds DB connection for 1-5+ seconds
- **Lock Contention**: Database locks held during external API calls
- **Partial Failures**: External API failure after DB save creates inconsistent state
- **Testing Complexity**: Need to mock entire transaction context

### ✅ Best Practice: Service Layer Transactions

```kotlin
@RestController
class ApplicationController {
    @PostMapping("/applications")
    fun createApplication(@RequestBody request: CreateApplicationRequest): ResponseEntity<ApplicationResponse> {
        // Extract user data in controller (database call happens here)
        val user = userService.getUserForRequest()
        val applicationId = applicationOrchestrator.createApplication(request, user)
        return ResponseEntity.created(URI.create("/applications/$applicationId"))
            .body(ApplicationResponse(applicationId))
    }
}

@Service
class ApplicationOrchestrator(
    private val queryService: ApplicationQueryService,
    private val commandService: ApplicationCommandService,
    private val externalValidationService: ExternalValidationService
) {
    // No @Transactional - orchestrates workflow
    fun createApplication(request: CreateApplicationRequest, user: User): UUID {
        // Phase 1: Read-only decision data (short transaction: ~10-50ms)
        val decisionData = queryService.loadDecisionData(request.crn, user)
        
        // Phase 2: External validation (outside any transaction)
        val validationResult = externalValidationService.validateOffender(request.crn, user)
        if (!validationResult.isValid) {
            throw ValidationException(validationResult.reason)
        }
        
        // Phase 3: Write transaction (short and focused: ~50-200ms)
        return commandService.createApplication(request, user, validationResult)
    }
}
```

**Benefits:**
- **Short Transactions**: Each transaction is focused and short-lived
- **Better Resource Utilization**: Database connections released quickly
- **Reduced Lock Contention**: Locks held for minimal time
- **Clear Separation**: Each layer has single responsibility
- **Easier Testing**: Each component testable in isolation

## OSIV Must Be Disabled: Explicit Fetch Plans

### ❌ Anti-Pattern: OSIV with Lazy Loading

```kotlin
// ❌ BAD: OSIV enabled, lazy loading everywhere
@RestController
class ApplicationController {
    @GetMapping("/applications/{id}")
    fun getApplication(@PathVariable id: UUID): ResponseEntity<ApplicationEntity> {
        // This works in tests but fails in production
        val application = applicationRepository.findById(id).get()
        
        // Lazy loading works because OSIV keeps session open
        val assessments = application.assessments // N+1 query!
        val createdBy = application.createdByUser // Another query!
        
        return ResponseEntity.ok(application)
    }
}
```

**Problems:**
- **N+1 Query Problem**: Each lazy relationship = separate database query
- **Connection Pool Exhaustion**: Each request holds DB connection for entire duration
- **Unpredictable Performance**: Same endpoint can take 10ms or 1000ms
- **Production Failures**: Works in tests, fails in production

### ✅ Best Practice: Explicit Fetch Plans

```kotlin
@RestController
class ApplicationController {
    @GetMapping("/applications/{id}")
    fun getApplication(@PathVariable id: UUID): ResponseEntity<ApplicationDto> {
        val application = applicationQueryService.getApplicationWithDetails(id)
        return ResponseEntity.ok(application)
    }
}

@Service
class ApplicationQueryService(
    private val applicationRepository: ApplicationRepository
) {
    @Transactional(readOnly = true)
    fun getApplicationWithDetails(id: UUID): ApplicationDto {
        // Explicit fetch plan - we control exactly what gets loaded
        return applicationRepository.findByIdWithAssessments(id)
            ?.toDto()
            ?: throw NotFoundException("Application $id not found")
    }
    
    @Transactional(readOnly = true)
    fun getApplicationSummary(id: UUID): ApplicationSummary {
        // Lightweight projection for list views
        return applicationRepository.findSummaryById(id)
            ?: throw NotFoundException("Application $id not found")
    }
    
    @Transactional(readOnly = true)
    fun getApplicationForUpdate(id: UUID): ApplicationEntity {
        // Minimal data for updates
        return applicationRepository.findByIdForUpdate(id)
            ?: throw NotFoundException("Application $id not found")
    }
}

interface ApplicationRepository : JpaRepository<ApplicationEntity, UUID> {
    // Full entity with relationships for details
    @EntityGraph(attributePaths = ["assessments", "createdByUser"])
    @Query("SELECT a FROM ApplicationEntity a WHERE a.id = :id")
    fun findByIdWithAssessments(@Param("id") id: UUID): ApplicationEntity?
    
    // Lightweight projection for lists
    @Query("""
        SELECT new com.example.ApplicationSummary(
            a.id, a.crn, a.status, a.createdAt, SIZE(a.assessments)
        )
        FROM ApplicationEntity a WHERE a.id = :id
    """)
    fun findSummaryById(@Param("id") id: UUID): ApplicationSummary?
    
    // Minimal data for updates
    @Query("SELECT a FROM ApplicationEntity a WHERE a.id = :id")
    fun findByIdForUpdate(@Param("id") id: UUID): ApplicationEntity?
}
```

**Benefits:**
- **Predictable Performance**: Known query count for each method
- **Better Resource Utilization**: Short read-only transactions
- **Explicit Intent**: Code shows exactly what data is needed
- **Production Reliability**: Consistent behavior across environments

## Unidirectional Aggregates: Avoid Bidirectional Relationships

### ❌ Anti-Pattern: Bidirectional Relationships

```kotlin
@Entity
class Application(
    @Id val id: UUID = UUID.randomUUID(),
    @OneToMany(mappedBy = "application", cascade = [CascadeType.ALL])
    var assessments: MutableList<Assessment> = mutableListOf()
)

@Entity
class Assessment(
    @Id val id: UUID = UUID.randomUUID(),
    @ManyToOne
    @JoinColumn(name = "application_id")
    var application: Application
)
```

**Problems:**
- **Sync Maintenance**: Both sides must be kept in sync
- **Orphan Data**: Easy to create orphaned records
- **Performance Issues**: Loading one side can trigger loading the other
- **Complex Lifecycle**: Hard to track ownership

### ✅ Best Practice: Unidirectional Aggregates

```kotlin
@Entity
class Application(
    @Id val id: UUID = UUID.randomUUID(),
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "application_id")
    private val _assessments: MutableList<Assessment> = mutableListOf()
) {
    val assessments: List<Assessment> get() = _assessments
    
    fun addAssessment(assessment: Assessment) {
        _assessments.add(assessment)
    }
    
    fun removeAssessment(assessmentId: UUID) {
        _assessments.removeIf { it.id == assessmentId }
    }
    
    fun getActiveAssessments(): List<Assessment> {
        return _assessments.filter { it.status == AssessmentStatus.ACTIVE }
    }
}

@Entity
class Assessment(
    @Id val id: UUID = UUID.randomUUID(),
    // No back-reference to Application
    val status: AssessmentStatus = AssessmentStatus.DRAFT
)
```

**Benefits:**
- **Single Source of Truth**: Aggregate root controls everything
- **No Orphan Data**: `orphanRemoval = true` automatically removes children
- **Better Performance**: No bidirectional loading issues
- **Clear Lifecycle**: Aggregate root owns the lifecycle

## External Calls Outside Transactions

### ❌ Anti-Pattern: External Calls in Transactions

```kotlin
@Transactional
fun createApplication(request: CreateApplicationRequest): UUID {
    val application = Application(request)
    applicationRepository.save(application)
    
    // External API call inside transaction (1-5 seconds!)
    val validationResult = externalApiClient.validateOffender(request.crn)
    if (!validationResult.isValid) {
        throw ValidationException(validationResult.reason)
    }
    
    application.updateWithValidation(validationResult)
    applicationRepository.save(application)
    
    return application.id
}
```

**Problems:**
- **Long Transaction Duration**: DB locks held during external API calls (1-5+ seconds)
- **Connection Pool Exhaustion**: DB connections held for entire external call duration
- **Partial Failure Handling**: External API failure after DB save creates inconsistency
- **Reduced Throughput**: Database resources tied up waiting for external systems

### ✅ Best Practice: External Calls Before/After Transactions

```kotlin
@Service
class ApplicationOrchestrator(
    private val queryService: ApplicationQueryService,
    private val commandService: ApplicationCommandService,
    private val externalValidationService: ExternalValidationService
) {
    // No @Transactional - orchestrates workflow
    fun createApplication(request: CreateApplicationRequest, user: User): UUID {
        // Phase 1: External validation BEFORE transaction
        val validationResult = externalValidationService.validateOffender(request.crn, user)
        if (!validationResult.isValid) {
            throw ValidationException(validationResult.reason)
        }
        
        // Phase 2: Short write transaction (only DB operations)
        return commandService.createApplication(request, user, validationResult)
    }
}

@Service
class ApplicationCommandService(
    private val applicationRepository: ApplicationRepository
) {
    @Transactional
    fun createApplication(
        request: CreateApplicationRequest,
        user: User,
        validationData: ValidationResult
    ): UUID {
        // Only database operations here - transaction is short
        val application = Application(request, validationData)
        applicationRepository.save(application)
        return application.id
    }
}
```

**Benefits:**
- **Short Transactions**: Only database operations in transactions (~50-200ms)
- **Better Resource Utilization**: DB connections released quickly
- **No Lock Contention**: Database locks not held during external calls
- **Clear Separation**: External I/O separated from database operations

## Outbox Pattern: Reliable Event Publishing

For asynchronous external communication (e.g., publishing domain events to message brokers), use the **Outbox Pattern** to ensure reliable delivery without blocking transactions.

### ❌ Anti-Pattern: Publishing Events Directly in Transactions

```kotlin
@Transactional
fun createApplication(request: CreateApplicationRequest, user: User): UUID {
    val application = Application(request)
    applicationRepository.save(application)
    
    // External event publishing inside transaction (1-5 seconds!)
    eventPublisher.publish(ApplicationCreatedEvent(application.id, user.id))
    
    return application.id
}
```

**Problems:**
- **Long Transaction Duration**: External publishing can take seconds, holding database connections
- **Connection Pool Exhaustion**: DB connections held during slow external operations
- **Event Loss Risk**: If publishing fails after transaction commits, event is lost
- **Lock Contention**: Database locks held during external operations

### ✅ Best Practice: Outbox Pattern

The outbox pattern stores events in the same transaction as domain changes, then a separate relay process publishes them asynchronously.

#### 1. Writing Events to Outbox in Same Transaction

```kotlin
@Entity
class OutboxMessage(
    @Id val id: UUID = UUID.randomUUID(),
    val aggregateType: String,
    val aggregateId: String,
    val eventType: String,
    @Column(columnDefinition = "jsonb") val payload: JsonNode,
    @Column(columnDefinition = "jsonb") val headers: JsonNode = objectMapper.createObjectNode(),
    val destination: String, // e.g., "sns:domain-events" or "sqs:orders-events"
    val dedupKey: String? = null,
    val partitionKey: String? = null,
    val createdAt: Instant = Instant.now(),
    val availableAt: Instant = Instant.now(),
    var attempts: Int = 0,
    var lastError: String? = null,
    var status: String = "PENDING" // PENDING|SENDING|SENT|FAILED
)

@Service
class ApplicationCommandService(
    private val applicationRepository: ApplicationRepository,
    private val outboxRepository: OutboxMessageRepository
) {
    @Transactional
    fun createApplication(request: CreateApplicationRequest, user: User): UUID {
        val application = Application(request)
        val savedApplication = applicationRepository.save(application)
        
        // Write domain events to outbox in same transaction
        application.domainEvents.forEach { event ->
            val outboxEvent = OutboxMessage(
                aggregateType = "Application",
                aggregateId = savedApplication.id.toString(),
                eventType = event.type, // e.g., "application.created"
                payload = objectMapper.valueToTree(event.payload),
                headers = buildHeaders(event), // traceId, causationId, etc.
                destination = "sns:domain-events",
                dedupKey = "${savedApplication.id}:${event.type}",
                partitionKey = savedApplication.id.toString(),
                status = "PENDING"
            )
            outboxRepository.save(outboxEvent)
        }
        
        // Clear domain events after persisting to outbox
        application.clearDomainEvents()
        
        return savedApplication.id
    }
}
```

#### 2. Outbox Table Schema

```sql
CREATE TABLE outbox_message (
  id               UUID PRIMARY KEY,
  aggregate_type   text NOT NULL,
  aggregate_id     text NOT NULL,
  event_type       text NOT NULL,
  payload          jsonb NOT NULL,
  headers          jsonb NOT NULL DEFAULT '{}'::jsonb,
  destination      text NOT NULL,
  dedup_key        text,
  partition_key    text,
  created_at       timestamptz NOT NULL DEFAULT now(),
  available_at     timestamptz NOT NULL DEFAULT now(),
  attempts         int NOT NULL DEFAULT 0,
  last_error       text,
  status           text NOT NULL DEFAULT 'PENDING'
);

-- Prevent duplicate logical events
CREATE UNIQUE INDEX IF NOT EXISTS outbox_dedup_idx
  ON outbox_message (event_type, dedup_key)
  WHERE dedup_key IS NOT NULL;

-- Fast relay scans
CREATE INDEX IF NOT EXISTS outbox_scan_idx
  ON outbox_message (status, available_at);
```

#### 3. Asynchronous Event Relay

A separate relay process reads committed outbox rows and publishes them to message brokers:

```kotlin
@Component
class OutboxRelay(
    private val jdbc: NamedParameterJdbcTemplate,
    private val snsClient: SnsClient,
    private val sqsClient: SqsAsyncClient,
    private val objectMapper: ObjectMapper
) {
    @Value("\${outbox.defaultTopic:}")
    private lateinit val defaultTopicArn = ""

    companion object {
        // Use SKIP LOCKED for safe concurrent workers, 
        private lateinit var claims = """
            WITH cte AS (
              SELECT id
              FROM outbox_message
              WHERE status = 'PENDING'
                AND available_at <= now()
              ORDER BY created_at
              FOR UPDATE SKIP LOCKED
              LIMIT 100
            )
            UPDATE outbox_message o
            SET status = 'SENDING', attempts = o.attempts + 1
            FROM cte
            WHERE o.id = cte.id
            RETURNING o.*
        """.trimIndent()
    }
    
    @Scheduled(fixedDelayString = "\${outbox.relay.fixedDelay:1000}")
    fun tick() {
        val batch = claimBatch() // status=PENDING→SENDING
        if (batch.isEmpty()) return

        val results = batch.map { msg ->
            when {
                msg.destination.startsWith("sns:") -> publishToSns(msg)
                msg.destination.startsWith("sqs:") -> publishToSqs(msg)
                else -> Result.failure(IllegalArgumentException("Unknown destination protocol: ${msg.destination}"))
            }
                .map { RelayResult.Success(msg.id) }
                .getOrElse { e -> RelayResult.Failure(msg.id, e) }
        }

        finalize(results)
    }

    private fun claimBatch(): List<OutboxMessage> {
        // this can be a hql query on a repository.
        return jdbc.query(claims, OutboxMessageRowMapper())
    }

    private fun publishToSns(m: OutboxMessage): Result<Unit> = runCatching {
        val topicArn = resolveTopicArn(m.destination)
        val messageJson = objectMapper.writeValueAsString(buildEventMessage(m))
        
        snsClient.publish {
            this.topicArn = topicArn
            this.message = messageJson
            messageAttributes = buildMessageAttributes(m)
        }
        Unit
    }

    private fun publishToSqs(m: OutboxMessage): Result<Unit> = runCatching {
        val queueUrl = parseQueueUrl(m.destination) // "sqs:orders-events" -> queue URL
        val req = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(m.payload.toString())
            .messageGroupId(m.partitionKey)    // for FIFO queues
            .messageDeduplicationId(m.dedupKey ?: m.id.toString())
            .build()
        sqsClient.sendMessage(req).get() // async client
        Unit
    }

    private fun finalize(results: List<RelayResult>) {
        val successIds = results.filterIsInstance<RelayResult.Success>().map { it.id }
        // Update status with sent

        val failures = results.filterIsInstance<RelayResult.Failure>()
        // update status with completed
    }
}

sealed class RelayResult {
    data class Success(val id: UUID): RelayResult()
    data class Failure(val id: UUID, val ex: Throwable): RelayResult()
}
```

**Key Points:**
- **SKIP LOCKED**: Allows multiple relay instances without conflicts
- **SENDING Status**: Prevents double-delivery if relay crashes mid-send
- **Same Transaction**: Events written to outbox in same transaction as domain changes

**Benefits:**
- **Short Transactions**: Only database operations in transactions
- **Reliable Delivery**: Events guaranteed to be written if transaction commits
- **Retry Capability**: Failed publishing can be retried automatically
- **No Event Loss**: Events persisted before external publishing
- **Better Resource Utilization**: DB connections released quickly

## Service Interface Design: Avoid Hidden Database Calls

### ❌ Anti-Pattern: Hidden Database Calls in Services

```kotlin
@RestController
class ApplicationController {
    @PostMapping("/applications")
    fun createApplication(@RequestBody request: CreateApplicationRequest): ResponseEntity<ApplicationResponse> {
        // Only pass the request - service will fetch user
        val applicationId = applicationOrchestrator.createApplication(request)
        return ResponseEntity.created(URI.create("/applications/$applicationId"))
            .body(ApplicationResponse(applicationId))
    }
}

@Service
class ApplicationOrchestrator(
    private val userService: UserService,  // Hidden dependency
    private val commandService: ApplicationCommandService
) {
    // Hidden database call - not obvious from interface
    fun createApplication(request: CreateApplicationRequest): UUID {
        val user = userService.getUserForRequest()  // Hidden database call inside service!
        
        // This creates an unexpected transaction boundary
        return commandService.createApplication(request, user)
    }
}
```

**Problems:**
- **Hidden Database Calls**: Service interface doesn't reveal database dependencies
- **Unclear Transaction Boundaries**: Database calls happen inside service methods, making transaction scope unclear
- **Performance Surprises**: Unexpected database queries during service execution
- **Harder to Test**: Need to mock database calls that aren't obvious from method signature
- **Transaction Management Issues**: Hidden calls can create nested transactions or unexpected transaction boundaries

### ✅ Best Practice: Explicit Data Passing

```kotlin
@RestController
class ApplicationController {
    @PostMapping("/applications")
    fun createApplication(@RequestBody request: CreateApplicationRequest): ResponseEntity<ApplicationResponse> {
        // Extract all needed data in controller (database calls happen here)
        val user = userService.getUserForRequest()
        val offender = offenderService.getOffender(request.crn)
        
        // Pass all extracted data to service - no hidden database calls
        val applicationId = applicationOrchestrator.createApplication(
            request = request,
            user = user,
            offender = offender
        )
        
        return ResponseEntity.created(URI.create("/applications/$applicationId"))
            .body(ApplicationResponse(applicationId))
    }
}

@Service
class ApplicationOrchestrator(
    private val queryService: ApplicationQueryService,
    private val commandService: ApplicationCommandService
) {
    // Clean interface: all data is passed in, no hidden database calls
    fun createApplication(
        request: CreateApplicationRequest,
        user: User,
        offender: Offender
    ): UUID {
        // No database calls here - all data is passed in
        // Transaction boundaries are clear and explicit
        return commandService.createApplication(request, user, offender)
    }
}
```

**Benefits:**
- **Explicit Dependencies**: Method signature shows exactly what data is needed
- **Clear Transaction Boundaries**: All database calls are explicit and controlled
- **No Hidden Calls**: Services don't make unexpected database queries
- **Better Performance**: Data fetching is controlled and can be optimized
- **Easier Testing**: Services can be tested with mock data without database setup
- **Predictable Transactions**: Transaction boundaries are clear from the code structure

## Complete Working Example

```kotlin
@RestController
@RequestMapping("/widgets")
class WidgetController(
    private val commands: WidgetCommandService,
    private val query: WidgetQueryService,
    private val orchestrator: WidgetOrchestrator,
    private val userService: UserService
) {
    @PostMapping
    fun create(@RequestBody @Valid req: CreateWidgetRequest): ResponseEntity<Map<String, Any>> {
        // Extract user data in controller (database call happens here)
        val user = userService.getUserForRequest()
        val id = commands.create(req, user)
        return ResponseEntity.status(HttpStatus.CREATED).body(mapOf("id" to id))
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): WidgetResponse = query.get(id).let {
        WidgetResponse(it.id!!, it.name, it.price, it.externalInfo)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody req: UpdateWidgetRequest) {
        val user = userService.getUserForRequest()
        commands.update(id, req, user)
    }

    // Orchestrated: API call outside TX, then short DB TX
    @PostMapping("/{id}/enrich")
    fun enrich(@PathVariable id: UUID) { 
        val user = userService.getUserForRequest()
        orchestrator.enrich(id, user) 
    }
}

// Query Service - Read-only operations
@Service
class WidgetQueryService(
    private val widgetRepo: WidgetRepository
) {
    @Transactional(readOnly = true)
    fun get(id: UUID): Widget =
        widgetRepo.findById(id).orElseThrow { NoSuchElementException("Widget $id not found") }
}

// Command Service - Write operations (receives user as parameter)
@Service
class WidgetCommandService(
    private val widgetRepo: WidgetRepository,
    private val eventRepo: DomainEventRepository
) {
    @Transactional
    fun create(req: CreateWidgetRequest, user: User): UUID {
        val w = Widget(name = req.name, price = req.price, createdBy = user.id)
        widgetRepo.save(w)
        enqueueEvent("WIDGET_CREATED", w.id!!, mapOf("name" to w.name, "userId" to user.id))
        return w.id!!
    }

    @Transactional
    fun update(id: UUID, req: UpdateWidgetRequest, user: User) {
        val w = widgetRepo.findById(id).orElseThrow { NoSuchElementException("Widget $id not found") }
        req.name?.let { w.name = it }
        req.price?.let { w.price = it }
        enqueueEvent("WIDGET_UPDATED", id, mapOf("name" to w.name, "userId" to user.id))
    }

    @Transactional
    fun applyEnrichment(id: UUID, info: String, user: User) {
        val w = widgetRepo.findById(id).orElseThrow { NoSuchElementException("Widget $id not found") }
        w.externalInfo = info
        enqueueEvent("WIDGET_ENRICHED", id, mapOf("externalInfo" to info, "userId" to user.id))
    }

    private fun enqueueEvent(type: String, aggregateId: UUID, payload: Map<String, Any>) {
        val evt = DomainEvent(
            type = type,
            aggregateType = "Widget",
            aggregateId = aggregateId.toString(),
            payload = jackson(payload),
            status = EventStatus.PENDING
        )
        eventRepo.save(evt)
    }
}

// Orchestrator - No @Transactional, coordinates workflow (receives user as parameter)
@Service
class WidgetOrchestrator(
    private val query: WidgetQueryService,
    private val commands: WidgetCommandService,
    private val api: ExternalApiClient
) {
    fun enrich(id: UUID, user: User) {
        // User data is passed in from controller - no database call here
        
        // Read current state (read-only txn starts/ends inside query.get())
        val widget = query.get(id)
        // External I/O OUTSIDE of any DB transaction
        val info = api.fetchInfo(widget.name)
        // Persist inside a short @Transactional method (through proxy)
        commands.applyEnrichment(id, info, user)
    }
}
```

## Key Takeaways

### Performance Benefits
- **Short transactions** = higher throughput, less lock contention
- **Explicit fetch plans** = predictable performance, no N+1 queries
- **No external calls in transactions** = better resource utilization

### Reliability Benefits
- **Unidirectional aggregates** = no orphan data, clear ownership
- **Short transactions** = reduced risk of deadlocks and timeouts
- **Explicit fetch plans** = consistent behavior across environments
- **Outbox pattern** = reliable event delivery without blocking transactions

### Maintainability Benefits
- **Clear separation of concerns** = easier to understand and modify
- **Explicit patterns** = no hidden surprises
- **Clear transaction boundaries** = easier to reason about performance

### Critical Rules
1. **Always Use Explicit Fetch Plans**: Never rely on lazy loading or OSIV
2. **Extract Data in Controllers**: Pass all needed data to services as parameters
3. **Keep Transactions Short**: Move external calls outside transactions
4. **Use Unidirectional Aggregates**: Avoid bidirectional relationships
5. **Service Layer Transactions**: Keep `@Transactional` in services, not controllers
6. **Use Outbox Pattern**: For reliable event publishing, write events to outbox in same transaction, publish asynchronously

The key insight is that **explicit is better than implicit**. When you make your intentions clear in the code, you get better performance, reliability, and maintainability. The small amount of extra code you write today saves hours of debugging and performance tuning tomorrow. 