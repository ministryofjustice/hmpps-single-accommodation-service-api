package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
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
  fun getCases(): ResponseEntity<List<CaseDto>> {
    val personDtos = caseQueryService.getCaseList()
    caseApplicationService.upsertCases(personDtos.map { it.crn })

    // TODO remove once we get data from sas_case table in the following PR
    val cases = personDtos.map { toCaseDto(it) }

    return ResponseEntity.ok(cases)
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @GetMapping("/cases")
  fun getCases(
    @RequestParam(required = false) searchTerm: String?,
    @RequestParam(required = false) status: Status?,
    @RequestParam(required = false) assignedTo: Long?,
    @RequestParam(required = false) riskLevel: RiskLevel?,
    @RequestParam(required = false) crns: List<String> = emptyList(),
  ): ResponseEntity<List<CaseDto>> {
    // TODO this allows for testing with multiple CRNs and will be removed in future.
    return if (crns.isNotEmpty()) {
      ResponseEntity.ok(caseQueryService.getCases(crns, riskLevel))
    } else {
      ResponseEntity.ok(emptyList())
    }
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @GetMapping("/cases/{crn}")
  fun getCase(@PathVariable crn: String): ResponseEntity<CaseDto> {
    val case = caseQueryService.getCase(crn)
    return ResponseEntity.ok(case)
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @GetMapping("/v2/cases")
  fun getCasesV2(
    @RequestParam(required = false) searchTerm: String?,
    @RequestParam(required = false) status: Status?,
    @RequestParam(required = false) assignedTo: Long?,
    @RequestParam(required = false) riskLevel: RiskLevel?,
    @RequestParam(required = false) crns: List<String> = emptyList(),
  ): ResponseEntity<ApiResponseDto<List<CaseDto>>> = if (crns.isNotEmpty()) {
    ResponseEntity.ok(caseQueryService.getCasesV2(crns, riskLevel))
  } else {
    ResponseEntity.ok(ApiResponseDto(data = emptyList()))
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER', 'POM')")
  @GetMapping("/v2/cases/{crn}")
  fun getCaseV2(@PathVariable crn: String): ResponseEntity<ApiResponseDto<CaseDto>> = ResponseEntity.ok(caseQueryService.getCaseV2(crn))
}
