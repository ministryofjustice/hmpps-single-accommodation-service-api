package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonTransformer.toPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto

@Service
class CaseQueryService(
  private val caseOrchestrationService: CaseOrchestrationService,
  private val userService: UserService,
  private val caseRepository: CaseRepository,
  private val eligibilityService: EligibilityService,
  private val dutyToReferQueryService: DutyToReferQueryService,
) {
  fun getCaseList(
    searchTerm: String? = null,
    riskLevel: RiskLevel? = null,
    teamCode: String? = null,
    assignedToUsername: String? = null,
  ): List<PersonDto> {
    val user = userService.authorizeAndRetrieveUser()

    return caseOrchestrationService.getCaseList(user.username)
      .cases
      .asSequence()
      .map { toPersonDto(it) }
      .filter {
        it.matchesSearch(searchTerm) &&
          it.matchesRosh(riskLevel) &&
          it.matchesAssignedTo(assignedToUsername) &&
          it.matchesTeam(teamCode)
      }.toList()
  }

  private fun PersonDto.matchesAssignedTo(assignedToUsername: String?): Boolean = when {
    assignedToUsername.isNullOrBlank() -> true
    assignedTo.username.equals(assignedToUsername, ignoreCase = true) -> true
    else -> false
  }

  private fun PersonDto.matchesTeam(teamCode: String?): Boolean = when {
    teamCode.isNullOrBlank() -> true
    teamCode.equals(teamCode, ignoreCase = true) -> true
    else -> false
  }

  private fun PersonDto.matchesRosh(riskLevel: RiskLevel?): Boolean = when {
    riskLevel == null -> true
    this is PersonalIdentifiable && roshLevel == riskLevel -> true
    else -> false
  }

  private fun PersonDto.matchesSearch(searchTerm: String?): Boolean = when {
    searchTerm.isNullOrBlank() -> true
    crn.equals(searchTerm, true) -> true
    nomsNumber?.equals(searchTerm, true) == true -> true
    this is PersonalIdentifiable && name.contains(searchTerm, true) -> true
    else -> false
  }

  fun getCases(personDtos: List<PersonDto>): List<CaseDto> {
    val crns = personDtos.map { it.crn }
    val caseEntitiesByCrn = caseRepository.mapByCrns(crns)

    return personDtos.map { personDto ->

      when (personDto) {
        is ExcludedPersonDto -> personDto.toCaseDto(caseEntity = null, eligibility = null)

        is RestrictedPersonDto, is FullPersonDto -> {
          val caseEntity = caseEntitiesByCrn[personDto.crn]
          val dutyToRefer = caseEntity?.let { dutyToReferQueryService.getDutyToRefer(it, personDto.crn) }
          val eligibility = getEligibility(
            crn = personDto.crn,
            gender = personDto.gender,
            caseEntity = caseEntity,
            dutyToRefer = dutyToRefer,
          )
          personDto.toCaseDto(caseEntity = caseEntitiesByCrn[personDto.crn], eligibility = eligibility)
        }
      }
    }
  }

  private fun getEligibility(
    crn: String,
    gender: String,
    caseEntity: CaseEntity?,
    dutyToRefer: DutyToReferDto?,
  ): EligibilityDto = eligibilityService.getEligibility(crn, gender, caseEntity, dutyToRefer)

  fun getCase(crn: String): ApiResponseDto<CaseDto> {
    val user = userService.authorizeAndRetrieveUser()
    val orchestrationResult = caseOrchestrationService.getCase(user.username, crn)
    val case = orchestrationResult.data.case?.let { toPersonDto(it) }.orThrowNotFound("crn" to crn)

    val caseOrchestrationDto = orchestrationResult.data
    val data = toCaseDto(
      case,
      caseOrchestrationDto.cpr,
      caseOrchestrationDto.roshDetails,
      caseOrchestrationDto.tier,
    )
    return toApiResponseDto(data = data, upstreamFailures = orchestrationResult.upstreamFailures)
  }
}
