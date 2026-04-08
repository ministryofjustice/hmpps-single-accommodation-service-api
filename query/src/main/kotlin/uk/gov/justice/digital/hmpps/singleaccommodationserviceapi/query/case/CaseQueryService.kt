package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonTransformer.toPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto

@Service
class CaseQueryService(
  private val caseOrchestrationService: CaseOrchestrationService,
  private val userService: UserService,
) {
  fun getCaseList(): ApiResponseDto<List<PersonDto>> {
    val user = userService.authorizeAndRetrieveUser()
    val caseList = caseOrchestrationService.getCaseList(user.username)
    return toApiResponseDto(
      data = caseList.data.cases.map { toPersonDto(it) },
      upstreamFailures = caseList.upstreamFailures,
    )
  }

  fun getCases(crns: List<String>, riskLevel: RiskLevel?): ApiResponseDto<List<CaseDto>> {
    val list = caseOrchestrationService.getCases(crns)
    val cases = list.map {
      toCaseDto(
        crn = it.data.crn,
        cpr = it.data.cpr,
        roshDetails = it.data.roshDetails,
        tier = it.data.tier,
        caseSummaries = it.data.cases,
      )
    }
      .filter { riskLevel == null || it.riskLevel == riskLevel }
      .sortedBy { it.name }
    return toApiResponseDto(
      data = cases,
      upstreamFailures = list.flatMap { it.upstreamFailures },
    )
  }

  fun getCase(crn: String): ApiResponseDto<CaseDto> {
    val case = caseOrchestrationService.getCase(crn)
    return toApiResponseDto(
      data = toCaseDto(crn, case.data.cpr, case.data.roshDetails, case.data.tier, case.data.cases),
      upstreamFailures = case.upstreamFailures,
    )
  }
}
