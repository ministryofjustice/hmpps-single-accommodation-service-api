package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import jakarta.validation.ValidationException
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.*
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.service.InboxEventService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.AdminArchiveCasesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.AdminBulkLoadCasesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.AdminBulkRefreshCasesService
import java.util.*

@RestController
class AdminJobController(
  private val adminBulkLoadCasesService: AdminBulkLoadCasesService,
  private val adminBulkRefreshCasesService: AdminBulkRefreshCasesService,
  private val inboxEventService: InboxEventService,
  private val adminArchiveCasesService: AdminArchiveCasesService,
) {

  @PreAuthorize("hasRole('ROLE_SAS_ADMIN_RW')")
  @PostMapping("/admin/bulk-load-cases")
  fun bulkLoadCases(@RequestBody request: BulkLoadCasesCommand): ResponseEntity<ApiResponseDto<BulkLoadCasesResultDto>> = ResponseEntity.ok(
    adminBulkLoadCasesService.bulkLoadCases(teamCodes = request.teamCodes, dryRun = request.dryRun),
  )

  @PreAuthorize("hasRole('ROLE_SAS_ADMIN_RW')")
  @PostMapping("/admin/bulk-refresh-cases-by-crn")
  fun bulkRefreshCasesByCrn(@RequestBody request: BulkRefreshCasesByCrnCommand): ResponseEntity<ApiResponseDto<BulkRefreshCasesResultDto>> = ResponseEntity.ok(
    adminBulkRefreshCasesService.bulkRefreshCasesByCrn(crns = request.crns, dryRun = request.dryRun),
  )

  @PreAuthorize("hasRole('ROLE_SAS_ADMIN_RW')")
  @PostMapping("/admin/bulk-refresh-all-cases")
  fun bulkRefreshAllCases(@RequestBody request: BulkRefreshAllCasesCommand): ResponseEntity<ApiResponseDto<BulkRefreshCasesResultDto>> = ResponseEntity.ok(
    adminBulkRefreshCasesService.buildRefreshAllCases(dryRun = request.dryRun),
  )

  @PreAuthorize("hasRole('ROLE_SAS_ADMIN_RW')")
  @PostMapping("/admin/archive-cases-by-crn")
  fun archiveCasesByCrn(@RequestBody request: ArchiveCasesByCrnCommand): ResponseEntity<ApiResponseDto<ArchiveCasesResultDto>> = ResponseEntity.ok(
    adminArchiveCasesService.archiveCasesByCrn(crns = request.crns, dryRun = request.dryRun),
  )

  @PreAuthorize("hasRole('ROLE_SAS_ADMIN_RW')")
  @PutMapping("/admin/replay-failed-inbox-events")
  fun replayFailedInboxEvents(
    @RequestParam(defaultValue = "false") replayAll: Boolean,
    @RequestBody(required = false) inboxEventIds: Set<UUID> = emptySet(),
  ): ResponseEntity<ApiResponseDto<ReplayFailedInboxEventsResponse>> {
    val (replayedMessageIds, replayedCount) = when (replayAll) {
      true -> {
        if (inboxEventIds.isNotEmpty()) {
          throw ValidationException("To replay all messages, inboxEventIds must be empty")
        }

        val ids = inboxEventService.findIdsByProcessedStatus(ProcessedStatus.FAILED)
        ids to inboxEventService.updateFailedToPending(ids)
      }

      false -> {
        if (inboxEventIds.isEmpty()) {
          throw ValidationException("To replay selected messages, inboxEventIds must be provided")
        }
        inboxEventIds to inboxEventService.updateFailedInboxEventStatus(inboxEventIds, ProcessedStatus.PENDING)
      }
    }
    return ResponseEntity.ok(
      ApiResponseDto(
        ReplayFailedInboxEventsResponse(
          replayAll = replayAll,
          replayedCount = replayedCount,
          replayedMessageIds = replayedMessageIds,
        ),
      ),
    )
  }

  @PreAuthorize("hasRole('ROLE_SAS_ADMIN_RW')")
  @PostMapping("/admin/replay-failed-refresh-requests")
  fun replayFailedRefreshRequests(): ResponseEntity<ApiResponseDto<ReplayFailedCaseRefreshDto>> = ResponseEntity.ok(
    adminBulkRefreshCasesService.replayFailedRefreshRequests(),
  )
}
