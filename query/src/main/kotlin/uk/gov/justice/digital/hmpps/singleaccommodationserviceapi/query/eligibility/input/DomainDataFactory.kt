package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.accommodation.AccommodationSummaryCalculator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.currentOutcome
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.historyExcludingCurrentOutcome

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
    val dutyToRefer = caseEntity?.let { dutyToReferQueryService.getDutyToRefer(caseEntity, crn) }

    val currentAccommodation = accommodationSummaryCalculator.calculateCurrentAccommodation(
      crn = crn,
      addresses = eligibilityOrchestrationDto.cpr?.addresses,
      prisoner = eligibilityOrchestrationDto.prisoner,
      cas1Residence = eligibilityOrchestrationDto.cas1Residence,
      cas3CurrentPremises = eligibilityOrchestrationDto.cas3CurrentPremises,
    )

    val currentAccommodationTypeEntity = accommodationTypeRepository.findAll().find { it.code == currentAccommodation?.type?.code }

    val nextAccommodations = accommodationSummaryCalculator.calculateNextAccommodations(
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

    val cas1ApplicationDomainData = eligibilityOrchestrationDto.cas1Application?.let {
      Cas1ApplicationDomainData(
        application = it,
        currentOutcome = it.currentOutcome,
        historyExcludingCurrentOutcome = it.historyExcludingCurrentOutcome,
      )
    }

    return DomainData(
      crn = crn,
      tierScore = eligibilityOrchestrationDto.tier?.tierScore,
      sex = eligibilityOrchestrationDto.cpr?.sex?.code,
      dob = eligibilityOrchestrationDto.cpr?.dateOfBirth,
      roshLevel = eligibilityOrchestrationDto.case?.roshLevel,
      cas1Application = cas1ApplicationDomainData,
      cas2Application = eligibilityOrchestrationDto.cas2Application,
      cas3Application = eligibilityOrchestrationDto.cas3Application,
      currentAccommodation = currentAccommodation,
      currentAccommodationTypeEntity = currentAccommodationTypeEntity,
      nextAccommodations = nextAccommodations,
      dutyToRefer = dutyToRefer,
      commissionedRehabilitativeServices = suitableCrsReferral,
      sexualOffenceRegistrations = eligibilityOrchestrationDto.sexualOffenceRegistrations,
    )
  }
}
