package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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

  @Operation(
    summary = "Get list of cases for the current user or selected team",
    description = """Returns the case list for the authenticated user. By default, returns cases allocated to the 
      current user. Supplying the teamCode parameter returns cases allocated to all users in that team. 
      Results can be further filtered by risk level and a free-text search term.""",
  )
  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/case-list")
  fun getCases(
    @Parameter(description = "Filter results to only cases with this risk level.")
    @RequestParam(required = false) riskLevel: RiskLevel?,
    @Parameter(description = "Free-text search term to filter cases by name or CRN.")
    @RequestParam(required = false) searchTerm: String?,
    @Parameter(description = "Team code to retrieve cases for all users in a team rather than the current user.")
    @RequestParam(required = false) teamCode: String?,
  ): ResponseEntity<ApiResponseDto<List<CaseDto>>> {
    val normalizedTeamCode = teamCode?.trim()?.takeIf { it.isNotEmpty() }
    val personDtos = caseQueryService.getCaseList(normalizedTeamCode)
    val upstreamFailures = personDtos.upstreamFailures.toMutableList()

    val filteredCaseList = caseQueryService.applyCaseListFilters(personDtos.data, searchTerm, riskLevel, normalizedTeamCode)
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
