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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CrnToPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseQueryService

@RestController
class CaseController(
  private val caseQueryService: CaseQueryService,
  private val caseApplicationService: CaseApplicationService,
) {

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/case-list")
  fun getCases(
    @RequestParam(required = false) riskLevel: RiskLevel?,
    @RequestParam(required = false) searchTerm: String?,
    @RequestParam(required = false) teamCode: String?,
  ): ResponseEntity<ApiResponseDto<List<CaseDto>>> {
    val personDtos = caseQueryService.getCaseList(teamCode)
    val upstreamFailures = personDtos.upstreamFailures.toMutableList()

    val filteredCaseList = caseQueryService.applyCaseListFilters(personDtos.data, searchTerm, riskLevel, teamCode)
    val crnsToPrisonNumbers = filteredCaseList.map { CrnToPrisonNumber(it.crn, it.nomsNumber) }
    // TODO: Change this to upsertCases after MVP
    caseApplicationService.createCases(crnsToPrisonNumbers)
    val caseDtos = caseQueryService.getCases(filteredCaseList)
    return ResponseEntity.ok(ApiResponseDto(data = caseDtos, upstreamFailures = upstreamFailures))
  }

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/cases/{crn}")
  fun getCase(@PathVariable crn: String): ResponseEntity<ApiResponseDto<CaseDto>> = ResponseEntity.ok(caseQueryService.getCase(crn))
}
