package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetailDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummariesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.accommodation.AccommodationSummaryCalculator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.accommodation.AccommodationTransformer.toAccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.accommodation.AccommodationTransformer.toAccommodationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.InOutStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationStatusRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto
import java.util.UUID

@Service
class AccommodationQueryService(
  private val accommodationOrchestrationService: AccommodationOrchestrationService,
  private val accommodationSummaryCalculator: AccommodationSummaryCalculator,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val accommodationTypeRepository: AccommodationTypeRepository,
  private val accommodationStatusRepository: AccommodationStatusRepository,
  private val caseRepository: CaseRepository,
) {
  private val excludedAddressStatuses = setOf(AddressStatusCode.PR.name, AddressStatusCode.PR1.name)

  private fun getPrisonNumber(crn: String): String? = caseRepository.findByCrn(crn)?.latestPrisonNumber()

  private fun getOrchestrationResult(crn: String): OrchestrationResultDto<AccommodationOrchestrationDto> {
    val prisonNumber = getPrisonNumber(crn)
    return accommodationOrchestrationService.getAccommodationOrchestration(crn, prisonNumber)
  }

  fun getCurrentAccommodation(crn: String): ApiResponseDto<AccommodationSummaryDto?> {
    val orchestrationResult = getOrchestrationResult(crn)
    return if (orchestrationResult.upstreamFailures.isNotEmpty()) {
      toApiResponseDto(
        data = null,
        upstreamFailures = orchestrationResult.upstreamFailures,
      )
    } else {
      val currentAccommodation = accommodationSummaryCalculator.calculateCurrentAccommodation(
        crn = crn,
        addresses = orchestrationResult.data.cpr?.addresses,
        prisoner = orchestrationResult.data.prisoner,
        cas1CurrentPremises = orchestrationResult.data.cas1CurrentPremises,
        cas3CurrentPremises = orchestrationResult.data.cas3CurrentPremises,
      )
      toApiResponseDto(
        data = currentAccommodation,
      )
    }
  }

  private fun getAccommodationSummaries(crn: String): Pair<AccommodationSummariesDto, List<UpstreamFailure>> {
    val prisonNumber = getPrisonNumber(crn)
    val orchestrationResult = accommodationOrchestrationService.getNextAccommodationOrchestration(crn, prisonNumber)
    val accommodationSummaries = accommodationSummaryCalculator.calculateAccommodationSummaries(
      crn = crn,
      addresses = orchestrationResult.data.cpr?.addresses,
      prisoner = orchestrationResult.data.prisoner,
      cas1CurrentPremises = orchestrationResult.data.cas1CurrentPremises,
      cas3CurrentPremises = orchestrationResult.data.cas3CurrentPremises,
      cas1Application = orchestrationResult.data.cas1Application,
      cas3Application = orchestrationResult.data.cas3Application,
    )
    return accommodationSummaries to orchestrationResult.upstreamFailures
  }

  fun getAccommodationSummariesResponse(crn: String): ApiResponseDto<AccommodationSummariesDto> {
    val (accommodationSummaries, upstreamFailures) = getAccommodationSummaries(crn)
    return toApiResponseDto(data = accommodationSummaries, upstreamFailures = upstreamFailures)
  }

  fun getNextAccommodation(crn: String): ApiResponseDto<AccommodationSummaryDto?> {
    val (accommodationSummaries, upstreamFailures) = getAccommodationSummaries(crn)
    return toApiResponseDto(
      data = accommodationSummaries.nextAccommodation,
      upstreamFailures = upstreamFailures,
    )
  }

  fun getAllAccommodations(crn: String): ApiResponseDto<List<AccommodationDetailDto>> {
    val orchestrationResult = accommodationOrchestrationService.getAccommodationOrchestration(crn)
    val allAccommodations = orchestrationResult.data.cpr?.let {
      it.addresses.map { toAccommodationDetail(crn, address = it) }
    } ?: emptyList()

    return toApiResponseDto(
      data = allAccommodations,
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }

  fun getAccommodationHistory(crn: String): ApiResponseDto<List<AccommodationSummaryDto>> {
    val prisonNumber = getPrisonNumber(crn)
    val orchestrationResult = accommodationOrchestrationService.getCprAndPrisonOrchestration(crn, prisonNumber)
    val data = orchestrationResult.data

    val prisonAddress = data.prisoner
      ?.takeIf { it.inOutStatus == InOutStatus.IN }
      ?.let { toAccommodationSummary(crn, prisoner = it) }

    val notProposedAddresses = data.cpr?.addresses?.filter { it.status.code !in excludedAddressStatuses }?.sortedByDescending { it.startDate }

    val accommodationHistory = listOfNotNull(prisonAddress) +
      notProposedAddresses?.map { toAccommodationSummary(crn, address = it) }.orEmpty()

    return toApiResponseDto(
      data = accommodationHistory,
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }

  fun getAccommodation(id: UUID): AccommodationDetailDto {
    val proposedAccommodationEntity = proposedAccommodationRepository.findByIdOrNull(id).orThrowNotFound("id" to id)
    val case = caseRepository.findWithIdentifiersById(proposedAccommodationEntity.caseId).orThrowNotFound("id" to proposedAccommodationEntity.id)
    val accommodationTypeEntity = proposedAccommodationEntity.accommodationTypeId?.let {
      accommodationTypeRepository.findByIdOrNull(it).orThrowNotFound("id" to it)
    }
    val accommodationStatusEntity = proposedAccommodationEntity.accommodationStatusId?.let {
      accommodationStatusRepository.findByIdOrNull(it).orThrowNotFound("id" to it)
    }
    return toAccommodationDetail(
      crn = case.latestCrn(),
      proposedAccommodationEntity = proposedAccommodationEntity,
      accommodationTypeEntity = accommodationTypeEntity,
      accommodationStatusEntity = accommodationStatusEntity,
    )
  }
}
