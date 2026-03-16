package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.CaseList
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService

@Service
class CaseService(
  private val caseOrchestrationService: CaseOrchestrationService,
  private val eligibilityService: EligibilityService,
  private val caseRepository: CaseRepository,
) {
  fun getCases(crns: List<String>, riskLevel: RiskLevel?): List<CaseDto> {
    val list = caseOrchestrationService.getCases(crns)
    return list.map {
      CaseTransformer.toCaseDto(
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

  fun getCaseList(caseList: CaseList): List<CaseDto> {
    val crns = caseList.cases.map { it.crn }

    val caseEntities = caseRepository.findByCrns(crns)

    return caseList.cases.map { caseListItem ->
      val caseEntity = caseEntities.first { it.crn == caseListItem.crn }
      val eligibility = eligibilityService.getEligibility(caseEntity.crn, caseEntity, caseListItem)

      CaseTransformer.toCaseDtoFromCaseEntity(
        caseEntity = caseEntity,
        eligibility = eligibility,
        caseListItem = caseListItem,
      )
    }
      .sortedBy { it.name }
  }

  fun getCase(crn: String): CaseDto {
    val caseOrch = caseOrchestrationService.getCase(crn)
    // at this point we should save the case to the database
    return CaseTransformer.toCaseDto(crn, caseOrch.cpr, caseOrch.roshDetails, caseOrch.tier, caseOrch.cases)
  }
}
