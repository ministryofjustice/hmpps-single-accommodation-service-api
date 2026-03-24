package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonTransformer.toPersonDto

@Service
class CaseQueryService(
  private val caseOrchestrationService: CaseOrchestrationService,
  private val userService: UserService,
) {
  fun getCaseList(): List<PersonDto> {
    val user = userService.authorizeAndRetrieveUser()

    // new pi endpoint
    val caseList = caseOrchestrationService.getCaseList(user.username)

    return caseList.cases.map { toPersonDto(it) }
  }

  fun getCases(crns: List<String>, riskLevel: RiskLevel?): List<CaseDto> {
    val list = caseOrchestrationService.getCases(crns)
    return list.map {
      toCaseDto(
        crn = it.crn,
        cpr = it.cpr,
        roshDetails = it.roshDetails,
        tier = it.tier,
        caseSummaries = it.cases,
      )
    }
      .filter { riskLevel == null || it.riskLevel == riskLevel }
      .sortedBy { it.name }
  }

  fun getCase(crn: String): CaseDto {
    val case = caseOrchestrationService.getCase(crn)
    return toCaseDto(crn, case.cpr, case.roshDetails, case.tier, case.cases)
  }
}
