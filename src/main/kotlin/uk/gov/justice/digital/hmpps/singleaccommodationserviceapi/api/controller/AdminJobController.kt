package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import jakarta.validation.ValidationException
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.BulkLoadCasesCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.BulkLoadCasesResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.BulkRefreshCasesByCrnCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.BulkRefreshCasesResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ReplayFailedInboxEventsResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.service.InboxEventService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.AdminBulkLoadCasesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.AdminBulkRefreshCasesService
import java.util.UUID

@RestController
class AdminJobController(
  private val adminBulkLoadCasesService: AdminBulkLoadCasesService,
  private val adminBulkRefreshCasesService: AdminBulkRefreshCasesService,
  private val inboxEventService: InboxEventService,
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
}
