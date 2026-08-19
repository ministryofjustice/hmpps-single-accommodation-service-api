package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.BulkLoadCasesCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.BulkLoadCasesResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.BulkLoadCasesService

@Hidden
@RestController
class AdminJobController(
  private val bulkLoadCasesService: BulkLoadCasesService,
) {

  @PreAuthorize("hasRole('ROLE_SAS_ADMIN_RW')")
  @PostMapping("/admin/bulk-load-cases")
  fun bulkLoadCases(@RequestBody request: BulkLoadCasesCommand): ResponseEntity<ApiResponseDto<BulkLoadCasesResultDto>> = ResponseEntity.ok(
    bulkLoadCasesService.bulkLoadCases(teamCodes = request.teamCodes, dryRun = request.dryRun),
  )
}
