package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CreateDtrCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.DutyToReferApplicationService

@RestController
class DutyToReferController(
  private val dutyToReferApplicationService: DutyToReferApplicationService,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @PreAuthorize("hasAnyRole('PROBATION', 'POM')")
  @GetMapping("/cases/{crn}/dtrs")
  fun getDutyToRefersByCrn(@PathVariable crn: String): ResponseEntity<List<DutyToReferDto>> {
    log.warn("/cases/{crn}/dtrs has not been implemented.")
    return ResponseEntity.ok(emptyList())
  }

  @PreAuthorize("hasAnyRole('PROBATION', 'POM')")
  @PostMapping("/cases/{crn}/dtr")
  fun create(
    @PathVariable crn: String,
    @RequestBody command: CreateDtrCommand,
  ): ResponseEntity<DutyToReferDto> {
    val createdDutyToRefer = dutyToReferApplicationService.createDutyToRefer(crn, command)
    return ResponseEntity(createdDutyToRefer, HttpStatus.CREATED)
  }
}
