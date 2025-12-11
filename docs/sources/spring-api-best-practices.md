# Best Practices & Guidelines

## Overview
This document provides clear guidelines and best practices for developing applications within the accommodation team, based on the patterns that have been successful and the lessons learned from our existing codebase.

## Controller Guidelines

### ✅ Recommended Pattern
Controllers should be thin and focused on HTTP concerns only:

```kotlin
@RestController
@RequestMapping("/api/v1/premises")
class PremisesController(
    private val premisesService: PremisesService,
    private val premisesTransformer: PremisesTransformer
) {
    
    @PostMapping
    fun createPremises(@RequestBody request: CreatePremisesRequest): ResponseEntity<PremisesResponse> {
        val premises = premisesService.createPremises(request)
        return ResponseEntity.ok(premisesTransformer.toResponse(premises))
    }
    
    @GetMapping("/{id}")
    fun getPremises(@PathVariable id: UUID): ResponseEntity<PremisesResponse> {
        val premises = premisesService.getPremises(id)
        return ResponseEntity.ok(premisesTransformer.toResponse(premises))
    }
}
```

### ❌ What to Avoid
- **@Transactional annotations** on controllers
- **Business logic** in controllers
- **Direct repository access** from controllers
- **Complex validation logic** in controllers

### Controller Responsibilities
1. **Request Validation**: Basic format and required field validation
2. **Authentication**: Extract user context from security context (if authentication fails, framework returns 401)
3. **Orchestration**: Call appropriate service methods
4. **Response Formatting**: Transform service responses to API format
5. **Error Handling**: Catch exceptions and return appropriate HTTP status codes

### Authentication vs Authorization (401 vs 403)

**401 Unauthorized** - Authentication failure ("Who are you?")
- Handled by the security framework **before** the request reaches the controller
- Occurs when:
   - Missing or invalid authentication token
   - Expired token
   - Malformed credentials
- **Controllers should NOT handle 401** - this is a framework concern

**403 Forbidden** - Authorization failure ("What can you do?")
- Handled by **services** when authenticated user lacks required permissions
- Occurs when:
   - User is authenticated but doesn't have required permission
   - User cannot access specific resource (e.g., different caseload)
- **Services throw authorization exceptions** that are caught by exception handlers

### ✅ Most Spring Idiomatic Approach: Extract User in Controller

**Option 1: Using `@AuthenticationPrincipal` (Most Idiomatic)**

```kotlin
@RestController
@RequestMapping("/api/v1/premises")
class PremisesController(
    private val premisesOrchestrator: PremisesOrchestrator,
    private val premisesTransformer: PremisesTransformer
) {
    
    @PostMapping
    fun createPremises(
        @RequestBody request: CreatePremisesRequest,
        @AuthenticationPrincipal user: User  // Spring injects authenticated user
    ): ResponseEntity<PremisesResponse> {
        // User is automatically extracted by Spring Security
        // If not authenticated, framework returns 401 before this point
        
        // Service handles authorization and throws exception if user lacks permission
        // Exception handler converts to 403
        val premises = premisesOrchestrator.createPremises(request, user)
        return ResponseEntity.ok(premisesTransformer.toResponse(premises))
    }
}
```

**Option 2: Manual Extraction (More MOJ Idiomatic)**

```kotlin
@RestController
@RequestMapping("/api/v1/premises")
class PremisesController(
    private val premisesOrchestrator: PremisesOrchestrator,
    private val userService: UserService,
    private val premisesTransformer: PremisesTransformer
) {
    
    @PostMapping
    fun createPremises(@RequestBody request: CreatePremisesRequest): ResponseEntity<PremisesResponse> {
        // Extract user - if not authenticated, framework returns 401 before this point
        val user = userService.getUserForRequest()
        
        // Service handles authorization and throws exception if user lacks permission
        // Exception handler converts to 403
        val premises = premisesOrchestrator.createPremises(request, user)
        return ResponseEntity.ok(premisesTransformer.toResponse(premises))
    }
}
```

**Why Extract in Controller?**

1. **Separation of Concerns**: Controllers handle HTTP/security concerns, services handle business logic
2. **Testability**: Services can be tested with mock users without SecurityContext
3. **Explicit Dependencies**: Service method signatures clearly show what data is required
4. **Reusability**: Services can be reused in non-HTTP contexts (batch jobs, CLI, etc.)
5. **Spring Idiomatic**: `@AuthenticationPrincipal` is the Spring Security recommended pattern, though possibly not used in MOJ

## Service Design Patterns

### Orchestrator Pattern (Recommended)
Use orchestrators to coordinate between multiple services:

```kotlin
@Service
class PremisesOrchestrator(
    private val queryService: PremisesQueryService,
    private val commandService: PremisesCommandService,
    private val authorizationService: AuthorizationService,
    private val externalApiClient: ExternalApiClient
) {
    
    // Authorization helper - throws exception if permission denied (converted to 403)
    private fun requirePermission(user: User, permission: String) {
        if (!authorizationService.hasPermission(user, permission)) {
            throw AccessDeniedException("User ${user.id} lacks permission: $permission")
        }
    }
    
    // Create operation with clear transaction boundary
    fun createPremises(request: CreatePremisesRequest, user: User): Premises {
        requirePermission(user, "CREATE_PREMISES")
        return commandService.createPremises(request, user)
    }
    
    // Complex operation with external API call
    fun enrichPremises(id: UUID, user: User): Premises {
        requirePermission(user, "ENRICH_PREMISES")
        
        // Phase 1: Read (within transaction)
        val premises = queryService.getPremises(id)
        
        // Phase 2: External API call (outside transaction)
        val externalInfo = externalApiClient.fetchPremisesInfo(premises.name)
        
        // Phase 3: Write (short, focused transaction)
        return commandService.applyEnrichment(id, externalInfo, user)
    }
    
    // Query operations (no authorization needed for reads)
    fun getPremisesSummaries(pageable: Pageable): Page<PremisesSummary> =
        queryService.getPremisesSummaries(pageable)
    
    fun getPremisesWithDetails(id: UUID): PremisesDetailResponse =
        queryService.getPremisesWithDetails(id)
}
```

### CQRS Pattern (Query/Command Separation)
Separate read and write operations:

```kotlin
@Service
@Transactional(readOnly = true)
class PremisesQueryService(
    private val premisesRepository: PremisesRepository
) {
    
    // Lightweight projection - no lazy loading issues
    fun getPremisesSummaries(pageable: Pageable): Page<PremisesSummary> {
        return premisesRepository.findSummaries(pageable)
    }
    
    // Explicit fetch plan - all data loaded within transaction
    fun getPremisesWithDetails(id: UUID): PremisesDetailResponse {
        return premisesRepository.findByIdWithDetails(id)
            ?.toDetailResponse()
            ?: throw NoSuchElementException("Premises $id not found")
    }
    
    // Minimal data for updates - focused transaction
    fun getPremisesForUpdate(id: UUID): Premises {
        return premisesRepository.findByIdForUpdate(id)
            ?: throw NoSuchElementException("Premises $id not found")
    }
}
```

```kotlin
@Service
@Transactional
class PremisesCommandService(
    private val premisesRepository: PremisesRepository,
    private val domainEventService: DomainEventService
) {
    
    fun createPremises(request: CreatePremisesRequest, user: User): Premises {
        val premises = Premises(
            name = request.name,
            address = request.address,
            createdBy = user.id
        )
        
        val savedPremises = premisesRepository.save(premises)
        
        // Write domain event in same transaction
        domainEventService.publish(
            PremisesCreatedEvent(
                premisesId = savedPremises.id,
                triggeredBy = user.id
            )
        )
        
        return savedPremises
    }
    
    fun updatePremises(id: UUID, request: UpdatePremisesRequest, user: User): Premises {
        val premises = premisesRepository.findByIdForUpdate(id)
            ?: throw NoSuchElementException("Premises $id not found")
        
        premises.update(request)
        val updatedPremises = premisesRepository.save(premises)
        
        domainEventService.publish(
            PremisesUpdatedEvent(
                premisesId = updatedPremises.id,
                triggeredBy = user.id
            )
        )
        
        return updatedPremises
    }
}
```

## Transaction Management

### ✅ Best Practices

1. **Keep Transactions Short**
   ```kotlin
   // Good: Short, focused transaction
   @Transactional
   fun createWidget(request: CreateWidgetRequest): Widget {
       val widget = Widget(request.name, request.description)
       return widgetRepository.save(widget)
   }
   ```

2. **External Calls Outside Transactions**
   ```kotlin
   // Good: External call outside transaction
   fun enrichWidget(id: UUID): Widget {
       val widget = queryService.getWidget(id) // Read transaction
       
       val externalInfo = externalApiClient.fetchInfo(widget.name) // No transaction
       
       return commandService.applyEnrichment(id, externalInfo) // Write transaction
   }
   ```

3. **Domain Events in Same Transaction**
   ```kotlin
   @Transactional
   fun createWidget(request: CreateWidgetRequest): Widget {
       val widget = widgetRepository.save(Widget(request.name))
       
       // Same transaction - ensures consistency
       domainEventService.publish(WidgetCreatedEvent(widget.id))
       
       return widget
   }
   ```

### ❌ Anti-Patterns

1. **Long-Running Transactions**
   ```kotlin
   // Bad: External API call within transaction
   @Transactional
   fun createWidget(request: CreateWidgetRequest): Widget {
       val widget = widgetRepository.save(Widget(request.name))
       
       // Blocks transaction for 2-3 seconds!
       val externalInfo = externalApiClient.fetchInfo(widget.name)
       
       return widget
   }
   ```

2. **Controller-Level Transactions**
   ```kotlin
   // Bad: Transaction at controller level
   @RestController
   @Transactional // Don't do this!
   class WidgetController {
       fun createWidget(request: CreateWidgetRequest): Widget {
           return widgetService.createWidget(request)
       }
   }
   ```

## Data Transformation

### Transformer Pattern
Keep transformations separate from entities:

```kotlin
@Component
class PremisesTransformer {
    
    fun toResponse(premises: Premises): PremisesResponse =
        PremisesResponse(
            id = premises.id,
            name = premises.name,
            address = premises.address,
            status = premises.status.name
        )
    
    fun toDetailResponse(premises: Premises): PremisesDetailResponse =
        PremisesDetailResponse(
            id = premises.id,
            name = premises.name,
            address = premises.address,
            status = premises.status.name,
            bookings = premises.bookings.map { toBookingResponse(it) },
            createdBy = premises.createdBy,
            createdAt = premises.createdAt
        )
    
    private fun toBookingResponse(booking: Booking): BookingResponse =
        BookingResponse(
            id = booking.id,
            startDate = booking.startDate,
            endDate = booking.endDate
        )
}
```

### Repository Projections
Use projections for efficient data retrieval:

```kotlin
interface PremisesRepository : JpaRepository<Premises, UUID> {
    
    // Lightweight projection
    fun findSummaries(pageable: Pageable): Page<PremisesSummary>
    
    // Detailed projection with joins
    @Query("""
        SELECT p FROM Premises p 
        LEFT JOIN FETCH p.bookings b 
        WHERE p.id = :id
    """)
    fun findByIdWithDetails(id: UUID): Premises?
    
    // Minimal data for updates
    @Query("SELECT p FROM Premises p WHERE p.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByIdForUpdate(id: UUID): Premises?
}
```

## Error Handling

### Consistent Error Responses
```kotlin
@ControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(404)
            .body(ErrorResponse("NOT_FOUND", ex.message))
    }
    
    @ExceptionHandler(ValidationException::class)
    fun handleValidation(ex: ValidationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(400)
            .body(ErrorResponse("VALIDATION_ERROR", ex.message))
    }
    
    // 403 Forbidden - thrown by services when user lacks permission
    // Note: 401 Unauthorized is handled by security framework, not here
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(403)
            .body(ErrorResponse("ACCESS_DENIED", ex.message))
    }
}
```

### Authorization Service Pattern
Services handle authorization checks and throw exceptions that are converted to 403:

```kotlin
@Service
class AuthorizationService(
    private val userPermissionService: UserPermissionService
) {
    fun hasPermission(user: User, permission: String): Boolean {
        return userPermissionService.hasPermission(user.id, permission)
    }
    
    fun requirePermission(user: User, permission: String) {
        if (!hasPermission(user, permission)) {
            throw AccessDeniedException("User ${user.id} lacks permission: $permission")
        }
    }
    
    fun requireResourceAccess(user: User, resourceId: UUID, resourceType: String) {
        if (!userPermissionService.canAccessResource(user.id, resourceId, resourceType)) {
            throw AccessDeniedException("User ${user.id} cannot access $resourceType: $resourceId")
        }
    }
}
```

### Result Pattern
Use sealed classes for type-safe error handling:

```kotlin
sealed class CasResult<out T> {
    data class Success<T>(val data: T) : CasResult<T>()
    data class Error(val code: String, val message: String) : CasResult<Nothing>()
}

fun createWidget(request: CreateWidgetRequest): CasResult<Widget> {
    return try {
        val widget = widgetRepository.save(Widget(request.name))
        CasResult.Success(widget)
    } catch (e: Exception) {
        CasResult.Error("CREATION_FAILED", e.message ?: "Unknown error")
    }
}
```

## Security Best Practices

### User Extraction: Controller vs Service

**✅ Spring Idiomatic: Extract User in Controller**

The most Spring idiomatic approach is to extract the authenticated user in the controller and pass it to the service:

```kotlin
// ✅ GOOD: Controller extracts user, passes to service
@RestController
class PremisesController(
    private val premisesOrchestrator: PremisesOrchestrator
) {
    @PostMapping
    fun createPremises(
        @RequestBody request: CreatePremisesRequest,
        @AuthenticationPrincipal user: User  // Spring injects user
    ): ResponseEntity<PremisesResponse> {
        val premises = premisesOrchestrator.createPremises(request, user)
        return ResponseEntity.ok(premisesTransformer.toResponse(premises))
    }
}

@Service
class PremisesOrchestrator(
    private val authorizationService: AuthorizationService
) {
    fun createPremises(request: CreatePremisesRequest, user: User): Premises {
        authorizationService.requirePermission(user, "CREATE_PREMISES")
        return commandService.createPremises(request, user)
    }
}
```

**❌ Anti-Pattern: Service Extracts User**

```kotlin
// ❌ BAD: Service extracts user from SecurityContext
@RestController
class PremisesController(
    private val premisesOrchestrator: PremisesOrchestrator
) {
    @PostMapping
    fun createPremises(@RequestBody request: CreatePremisesRequest): ResponseEntity<PremisesResponse> {
        // Service will extract user internally
        val premises = premisesOrchestrator.createPremises(request)
        return ResponseEntity.ok(premisesTransformer.toResponse(premises))
    }
}

@Service
class PremisesOrchestrator(
    private val userService: UserService  // Hidden dependency
) {
    fun createPremises(request: CreatePremisesRequest): Premises {
        val user = userService.getUserForRequest()  // Hidden SecurityContext dependency
        authorizationService.requirePermission(user, "CREATE_PREMISES")
        return commandService.createPremises(request, user)
    }
}
```

**Why Extract in Controller?**

1. **Spring Idiomatic**: `@AuthenticationPrincipal` is the Spring Security recommended pattern
2. **Explicit Dependencies**: Service method signatures show all required data
3. **Testability**: Services can be tested with mock users (no SecurityContext mocking needed)
4. **Separation of Concerns**: Controllers handle HTTP/security, services handle business logic
5. **Reusability**: Services can be used in non-HTTP contexts (batch jobs, CLI tools)
6. **Performance**: User extraction happens once in controller, not hidden in services

### Authentication (401) vs Authorization (403)

**Key Principle**:
- **401 Unauthorized** = Framework concern (handled before controllers)
- **403 Forbidden** = Service concern (handled by business logic)

**Recommended Flow**:
1. **Security Framework** validates authentication token → returns 401 if invalid
2. **Controller** extracts authenticated user from security context
3. **Service** performs authorization checks → throws `AccessDeniedException` if denied
4. **Exception Handler** converts `AccessDeniedException` → 403 response

**Why This Separation?**
- **401 is infrastructure** - handled uniformly by security framework
- **403 is business logic** - depends on permissions, resource ownership, caseloads, etc.
- **Services know the domain** - they understand what "access" means in context
- **Testability** - services can be tested without security framework

### Authorization Patterns

```kotlin
// ✅ GOOD: Controller extracts user, service checks authorization
@RestController
class PremisesController(
    private val premisesOrchestrator: PremisesOrchestrator,
    private val userService: UserService
) {
    @PostMapping
    fun createPremises(@RequestBody request: CreatePremisesRequest): ResponseEntity<PremisesResponse> {
        // Extract user - if not authenticated, framework already returned 401
        val user = userService.getUserForRequest()
        
        // Service checks authorization and throws AccessDeniedException if denied
        val premises = premisesOrchestrator.createPremises(request, user)
        return ResponseEntity.ok(premisesTransformer.toResponse(premises))
    }
}

// ✅ GOOD: Service performs authorization check
@Service
class PremisesOrchestrator(
    private val authorizationService: AuthorizationService
) {
    fun createPremises(request: CreatePremisesRequest, user: User): Premises {
        // Business logic determines authorization requirements
        authorizationService.requirePermission(user, "CREATE_PREMISES")
        
        // Additional context-specific checks
        if (request.requiresSpecialAccess) {
            authorizationService.requirePermission(user, "CREATE_PREMISES_ADVANCED")
        }
        
        return commandService.createPremises(request, user)
    }
}
```

```kotlin
// ❌ BAD: Authorization logic in controller
@RestController
class PremisesController {
    @PostMapping
    fun createPremises(@RequestBody request: CreatePremisesRequest): ResponseEntity<PremisesResponse> {
        val user = userService.getUserForRequest()
        
        // Don't do authorization checks in controllers
        if (!user.hasPermission("CREATE_PREMISES")) {
            return ResponseEntity.status(403).build() // ❌ Business logic in controller
        }
        
        return ResponseEntity.ok(premisesService.createPremises(request, user))
    }
}
```

## Summary

Following these best practices will help ensure:
- **Maintainable code** with clear separation of concerns
- **Reliable transactions** with proper boundaries
- **Good performance** through optimised data access
- **Consistent error handling** across the application
- **Testable code** with proper dependency injection
- **Secure applications** with proper validation and authorization
- **Clear security boundaries** between authentication (401) and authorization (403)