package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
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
  @PostMapping("/admin/replay-failed-inbox-events")
  fun replayFailedInboxEvents(
    @RequestParam replayAll: Boolean = false,
    @RequestBody inboxEventIds: Set<UUID> = emptySet(),
  ): ResponseEntity<ApiResponseDto<ReplayFailedInboxEventsResponse>> {
    val replayedCount = when (replayAll) {
      true -> inboxEventService.updateAllFailedToPending()
      false -> inboxEventService.updateFailedInboxEventStatus(inboxEventIds, ProcessedStatus.PENDING)
    }
    return ResponseEntity.ok(
      ApiResponseDto(ReplayFailedInboxEventsResponse(replayAll = replayAll, replayedCount = replayedCount)),
    )
  }
}
