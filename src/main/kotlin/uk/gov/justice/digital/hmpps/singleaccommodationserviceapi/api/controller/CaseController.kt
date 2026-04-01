package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PartialSuccessResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDto

@RestController
class CaseController(
  private val caseQueryService: CaseQueryService,
  private val caseApplicationService: CaseApplicationService,
) {

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @GetMapping("/case-list")
  fun getCases(): ResponseEntity<PartialSuccessResponseDto<List<CaseDto>>> {
    val result = caseQueryService.getCaseList()
    caseApplicationService.upsertCases(result.data.map { it.crn })

    // TODO remove once we get data from sas_case table in the following PR
    val cases = result.data.map { toCaseDto(it) }

    return ResponseEntity.ok(PartialSuccessResponseDto(data = cases, upstreamFailures = result.upstreamFailures))
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @GetMapping("/cases")
  fun getCases(
    @RequestParam(required = false) searchTerm: String?,
    @RequestParam(required = false) status: Status?,
    @RequestParam(required = false) assignedTo: Long?,
    @RequestParam(required = false) riskLevel: RiskLevel?,
    @RequestParam(required = false) crns: List<String> = emptyList(),
  ): ResponseEntity<PartialSuccessResponseDto<List<CaseDto>>> {
    // TODO this allows for testing with multiple CRNs and will be removed in future.
    return if (crns.isNotEmpty()) {
      ResponseEntity.ok(caseQueryService.getCases(crns, riskLevel))
    } else {
      ResponseEntity.ok(PartialSuccessResponseDto(data = emptyList()))
    }
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @GetMapping("/cases/{crn}")
  fun getCase(@PathVariable crn: String): ResponseEntity<PartialSuccessResponseDto<CaseDto>> = ResponseEntity.ok(caseQueryService.getCase(crn))
}
