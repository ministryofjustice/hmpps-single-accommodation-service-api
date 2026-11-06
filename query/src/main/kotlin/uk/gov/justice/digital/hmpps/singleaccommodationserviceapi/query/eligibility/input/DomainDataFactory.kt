package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.accommodation.AccommodationSummaryCalculator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService

@Service
class DomainDataFactory(
  private val accommodationSummaryCalculator: AccommodationSummaryCalculator,
  private val accommodationTypeRepository: AccommodationTypeRepository,
  private val dutyToReferQueryService: DutyToReferQueryService,
) {

  fun buildDomainData(
    crn: String,
    eligibilityOrchestrationDto: EligibilityOrchestrationDto,
    caseEntity: CaseEntity?,
  ): DomainData {
    val accommodationTypes = accommodationTypeRepository.findAll()

    val dutyToRefer = caseEntity?.let { dutyToReferQueryService.getDutyToRefer(caseEntity, crn) }

    val currentAccommodation = accommodationSummaryCalculator.calculateCurrentAccommodation(
      crn = crn,
      addresses = eligibilityOrchestrationDto.cpr?.addresses,
      prisoner = eligibilityOrchestrationDto.prisoner,
      cas1CurrentPremises = eligibilityOrchestrationDto.cas1CurrentPremises,
      cas3CurrentPremises = eligibilityOrchestrationDto.cas3CurrentPremises,
    )

    val nextAccommodations = accommodationSummaryCalculator.calculateNextAccommodationsV2(
      crn,
      addresses = eligibilityOrchestrationDto.cpr?.addresses,
      cas1Application = eligibilityOrchestrationDto.cas1Application,
      cas3Application = eligibilityOrchestrationDto.cas3Application,
      currentAccommodation = currentAccommodation,
    )

    val suitableCrsReferral = eligibilityOrchestrationDto.commissionedRehabilitativeServices
      ?.filter { it.status == CrsReferralStatus.LIVE }
      ?.filter { it.sentAt != null }
      ?.maxByOrNull { it.sentAt!! }

    return DomainData(
      crn = crn,
      cpr = eligibilityOrchestrationDto.cpr,
      tier = eligibilityOrchestrationDto.tier,
      case = eligibilityOrchestrationDto.case,
      cas1Application = eligibilityOrchestrationDto.cas1Application,
      cas2Application = eligibilityOrchestrationDto.cas2Application,
      cas3Application = eligibilityOrchestrationDto.cas3Application,
      currentAccommodation = currentAccommodation,
      nextAccommodations = nextAccommodations,
      dutyToRefer = dutyToRefer,
      commissionedRehabilitativeServices = suitableCrsReferral,
      accommodationTypes = accommodationTypes,
      sexualOffenceRegistrations = eligibilityOrchestrationDto.sexualOffenceRegistrations,
    )
  }
}
