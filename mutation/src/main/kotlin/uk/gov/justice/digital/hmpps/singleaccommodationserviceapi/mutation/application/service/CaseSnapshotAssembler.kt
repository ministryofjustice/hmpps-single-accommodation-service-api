package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.accommodation.AccommodationSummaryCalculator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate

@Service
class CaseSnapshotAssembler(
  private val accommodationSummaryCalculator: AccommodationSummaryCalculator,
) {

  fun upsertCase(aggregate: CaseAggregate, caseMutationOrchestrationDto: CaseMutationOrchestrationDto): CaseAggregate {
    val accommodationSummaries = accommodationSummaryCalculator.calculateAccommodationSummaries(
      crn = caseMutationOrchestrationDto.crn,
      addresses = caseMutationOrchestrationDto.cpr?.addresses,
      prisoner = caseMutationOrchestrationDto.prisoner,
      cas1CurrentPremises = caseMutationOrchestrationDto.cas1CurrentPremises,
      cas3CurrentPremises = caseMutationOrchestrationDto.cas3CurrentPremises,
      cas1Application = caseMutationOrchestrationDto.cas1Application,
      cas3Application = caseMutationOrchestrationDto.cas3Application,
    )

    return aggregate.upsertCase(
      tierScore = caseMutationOrchestrationDto.tier?.tierScore,
      firstName = caseMutationOrchestrationDto.cpr?.firstName,
      lastName = caseMutationOrchestrationDto.cpr?.lastName,
      dateOfBirth = caseMutationOrchestrationDto.cpr?.dateOfBirth,
      currentAccommodation = accommodationSummaries.currentAccommodation,
      nextAccommodation = accommodationSummaries.nextAccommodation,
      accommodationStatus = accommodationSummaries.caseAccommodationStatus,
    )
  }
}
