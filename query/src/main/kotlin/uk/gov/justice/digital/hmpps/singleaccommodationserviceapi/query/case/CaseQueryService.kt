package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonTransformer.toPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto

@Service
class CaseQueryService(
  private val caseOrchestrationService: CaseOrchestrationService,
  private val userService: UserService,
  private val caseRepository: CaseRepository,
  private val eligibilityService: EligibilityService,
) {
  fun getCaseList(): List<PersonDto> {
    val user = userService.authorizeAndRetrieveUser()

    // new pi endpoint
    val caseList = caseOrchestrationService.getCaseList(user.username)

    return caseList.cases.map { toPersonDto(it) }
  }

  fun getCases(personDtos: List<PersonDto>): List<CaseDto> {
    val crns = personDtos.map { it.crn }
    val caseEntities = caseRepository.findByCrns(crns)

    return personDtos.map { personDto ->
      val caseEntity = caseEntities.find { entity ->
        entity.caseIdentifiers.any { it.identifier == personDto.crn && it.identifierType == IdentifierType.CRN }
      }
      val eligibilityDto = eligibilityService.getEligibility(personDto, caseEntity)
      toCaseDto(
        personDto,
        caseEntity,
        eligibilityDto,
      )
    }
  }

  fun getCase(crn: String): ApiResponseDto<CaseDto> {
    val orchestrationResult = caseOrchestrationService.getCase(crn)
    val case = orchestrationResult.data
    return toApiResponseDto(
      data = toCaseDto(crn, case.cpr, case.roshDetails, case.tier, case.cases),
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }
}
