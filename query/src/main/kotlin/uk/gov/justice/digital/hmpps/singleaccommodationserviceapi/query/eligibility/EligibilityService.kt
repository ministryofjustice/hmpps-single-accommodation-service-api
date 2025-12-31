package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RulesEngine

@Service
class EligibilityService(
  private val eligibilityOrchestrationService: EligibilityOrchestrationService,
  private val cas1RuleSet: Cas1RuleSet,
  @Qualifier("defaultRulesEngine")
  private val engine: RulesEngine,
) {

  fun getEligibility(crn: String): EligibilityDto {
    val data = getDomainData(crn)

    val cas1 = calculateEligibilityForCas1(data)

    return EligibilityDto(
      crn,
      cas1,
      caseStatus = null,
      caseActions = listOf(),
      cas2Hdc = null,
      cas2PrisonBail = null,
      cas2CourtBail = null,
      cas3 = null,
    )
  }

  fun calculateEligibilityForCas1(data: DomainData) = if (data.cas1Application != null) {
    ServiceResult(
      serviceStatus = data.cas1Application.toServiceStatus(),
      actions = data.cas1Application.buildActions(),
    )
  } else {
    engine.execute(cas1RuleSet, data)
  }

  fun getDomainData(crn: String): DomainData {
    val eligibilityOrchestrationDto = eligibilityOrchestrationService.getData(crn)

    val prisonerNumbers = eligibilityOrchestrationDto.cpr.identifiers?.prisonNumbers ?: error("No prisoner numbers found for crn: $crn")

    val prisonerData = eligibilityOrchestrationService.getPrisonerData(prisonerNumbers)

    return DomainData(
      crn,
      eligibilityOrchestrationDto.cpr,
      eligibilityOrchestrationDto.tier,
      prisonerData,
      eligibilityOrchestrationDto.cas1Application,
    )
  }

  fun Cas1ApplicationStatus.toServiceStatus() = when (this) {
    Cas1ApplicationStatus.PLACEMENT_ALLOCATED -> null
    Cas1ApplicationStatus.AWAITING_ASSESSMENT -> ServiceStatus.AWAITING_ASSESSMENT
    Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT -> ServiceStatus.UNALLOCATED_ASSESSMENT
    Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS -> ServiceStatus.ASSESSMENT_IN_PROGRESS
    Cas1ApplicationStatus.AWAITING_PLACEMENT -> ServiceStatus.AWAITING_PLACEMENT
    Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION -> ServiceStatus.REQUEST_FOR_FURTHER_INFORMATION
    Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST -> ServiceStatus.PENDING_PLACEMENT_REQUEST
  }

  fun Cas1PlacementStatus.toServiceStatus() = when (this) {
    Cas1PlacementStatus.UPCOMING -> ServiceStatus.UPCOMING_PLACEMENT
    Cas1PlacementStatus.ARRIVED -> ServiceStatus.ARRIVED
    Cas1PlacementStatus.DEPARTED -> ServiceStatus.DEPARTED
    Cas1PlacementStatus.NOT_ARRIVED -> ServiceStatus.NOT_ARRIVED
    Cas1PlacementStatus.CANCELLED -> ServiceStatus.CANCELLED
  }

  fun Cas1Application.toServiceStatus() = applicationStatus.toServiceStatus()
    ?: placementStatus?.toServiceStatus()
    ?: error("Null Placement Status")
}
