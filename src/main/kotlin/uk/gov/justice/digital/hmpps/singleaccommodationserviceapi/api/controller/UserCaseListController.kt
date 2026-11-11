package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.controller

import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.ValidationException
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PeopleType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseListType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.UserCaseListApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.usercaselist.UserCaseListQueryService

private val CRN_REGEX = Regex("(?i)^[A-Z][0-9]{6}$")
private const val CRN_FORMAT_MESSAGE = "Enter a valid format for all CRNs. For example, D016821"

@Validated
@RestController
class UserCaseListController(
  private val userService: UserService,
  private val caseApplicationService: CaseApplicationService,
  private val userCaseListApplicationService: UserCaseListApplicationService,
  private val userCaseListQueryService: UserCaseListQueryService,
) {

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/case-list/custom")
  fun getUserCaseList(
    @Parameter(description = "Filter results to only cases with this risk level.")
    @RequestParam(required = false) riskLevel: RiskLevel?,
    @Parameter(description = "Free-text search term to filter cases by name or CRN.")
    @RequestParam(required = false) searchTerm: String?,
    @Parameter(description = "Team code to retrieve cases for all users in a team rather than the current user.")
    @RequestParam(required = false) teamCode: String?,
    @Parameter(description = "People type to filter based on tabs")
    @RequestParam(required = false) peopleType: PeopleType?,
  ): ResponseEntity<ApiResponseDto<List<CaseDto>>> = ResponseEntity.ok(
    userCaseListQueryService.getUserCaseList(
      listType = CaseListType.CUSTOM,
      searchTerm = searchTerm,
      riskLevel = riskLevel,
      teamCode = teamCode,
      peopleType = peopleType,
    ),
  )

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/case-list/search-history")
  fun getSearchHistoryCaseList(): ResponseEntity<ApiResponseDto<List<CaseDto>>> = ResponseEntity.ok(
    userCaseListQueryService.getUserCaseList(listType = CaseListType.SEARCH_HISTORY),
  )

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @GetMapping("/case-list/custom/crns")
  fun getUserCaseListCrns(): ResponseEntity<ApiResponseDto<List<String>>> = ResponseEntity.ok(userCaseListQueryService.getUserCaseListCrns(CaseListType.CUSTOM))

  @PreAuthorize("hasAnyRole('SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER')")
  @PostMapping("/case-list/custom")
  @ResponseStatus(HttpStatus.CREATED)
  fun createUserCaseList(
    @RequestBody
    @Size(min = 1, max = 500, message = "Enter 500 CRNs or fewer")
    crns: List<String>,
  ): ResponseEntity<Void> {
    val normalisedCrns = crns.map { it.trim().uppercase() }
    val invalidCrns = normalisedCrns.filterNot { CRN_REGEX.matches(it) }

    if (invalidCrns.isNotEmpty()) {
      val messageSuffix = if (invalidCrns.size > 1) "are not valid CRN formats" else "is not a valid CRN format"
      throw ValidationException("$CRN_FORMAT_MESSAGE. ${invalidCrns.joinToString()} $messageSuffix")
    }

    val user = userService.authorizeAndRetrieveUser()
    val distinctCrns = normalisedCrns.distinct()
    caseApplicationService.createValidatedCases(distinctCrns)
    userCaseListApplicationService.createUserCaseList(user.id, CaseListType.CUSTOM, distinctCrns)
    return ResponseEntity(HttpStatus.CREATED)
  }
}
