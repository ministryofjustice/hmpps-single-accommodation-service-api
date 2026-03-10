package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.DutyToReferApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import java.util.UUID

@RestController
class DutyToReferController(
  private val dutyToReferApplicationService: DutyToReferApplicationService,
  private val dutyToReferQueryService: DutyToReferQueryService,
) {

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @GetMapping("/cases/{crn}/dtr")
  fun getDutyToRefer(@PathVariable crn: String): ResponseEntity<DutyToReferDto> {
    val dutyToRefer = dutyToReferQueryService.getDutyToRefer(crn)
    return ResponseEntity.ok(dutyToRefer)
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @PostMapping("/cases/{crn}/dtr")
  fun create(
    @PathVariable crn: String,
    @RequestBody command: DtrCommand,
  ): ResponseEntity<DutyToReferDto> {
    val createdDutyToRefer = dutyToReferApplicationService.createDutyToRefer(crn, command)
    return ResponseEntity(createdDutyToRefer, HttpStatus.CREATED)
  }

  @PreAuthorize("hasRole('ROLE_SINGLE_ACCOMMODATION_SERVICE__ACCOMMODATION_DATA_DOMAIN')")
  @GetMapping("/duty-to-refers/{id}")
  fun getById(@PathVariable id: UUID): ResponseEntity<DutyToReferDto> {
    val dutyToRefer = dutyToReferQueryService.getDutyToRefer(id)
    return ResponseEntity.ok(dutyToRefer)
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @PutMapping("/cases/{crn}/dtr/{id}")
  fun update(
    @PathVariable crn: String,
    @PathVariable id: UUID,
    @RequestBody command: DtrCommand,
  ): ResponseEntity<DutyToReferDto> {
    val updatedDutyToRefer = dutyToReferApplicationService.updateDutyToRefer(crn, id, command)
    return ResponseEntity.ok(updatedDutyToRefer)
  }
}
