package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationStatusRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationSummaryTransformer.toAccommodationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto
import java.util.UUID

@Service
class AccommodationQueryService(
  private val accommodationOrchestrationService: AccommodationOrchestrationService,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val accommodationTypeRepository: AccommodationTypeRepository,
  private val accommodationStatusRepository: AccommodationStatusRepository,
  private val caseRepository: CaseRepository,
) {
  fun getCurrentAccommodation(crn: String): ApiResponseDto<AccommodationSummaryDto?> {
    val orchestrationResult = accommodationOrchestrationService.getCorePersonRecordByCrn(crn)
    val currentAccommodation = orchestrationResult.data.cpr?.addresses?.let {
      getCurrentAccommodation(crn, addresses = it)
    }
    return toApiResponseDto(
      data = currentAccommodation,
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }

  fun getCurrentAccommodation(crn: String, addresses: List<CanonicalAddress>): AccommodationSummaryDto? = addresses
    .firstOrNull { it.status.code == AddressStatusCode.M.name }
    ?.let { toAccommodationSummary(crn, address = it) }

  // TODO implement this once we understand how next accommodation is calculated - will also contain cas1 and cas3 arguments
  fun getNextAccommodation(crn: String, addresses: List<CanonicalAddress>): AccommodationSummaryDto? = null

  fun getAccommodationHistory(crn: String): ApiResponseDto<List<AccommodationSummaryDto>> {
    val orchestrationResult = accommodationOrchestrationService.getCorePersonRecordByCrn(crn)
    val corePersonRecord = orchestrationResult.data
    val nonProposedAddresses = corePersonRecord.cpr?.addresses
      ?.filter {
        it.status.code != AddressStatusCode.PR.name &&
          it.status.code != AddressStatusCode.PR1.name
      }
    return generateAccommodationHistoryResponse(
      crn,
      addresses = nonProposedAddresses,
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }

  private fun generateAccommodationHistoryResponse(
    crn: String,
    addresses: List<CanonicalAddress>?,
    upstreamFailures: List<UpstreamFailure>,
  ): ApiResponseDto<List<AccommodationSummaryDto>> {
    val accommodationHistory = addresses?.map { toAccommodationSummary(crn, address = it) } ?: emptyList()
    return toApiResponseDto(
      data = accommodationHistory,
      upstreamFailures = upstreamFailures,
    )
  }

  fun getAccommodation(id: UUID): AccommodationSummaryDto {
    val proposedAccommodationEntity = proposedAccommodationRepository.findByIdOrNull(id).orThrowNotFound("id" to id)
    val case = caseRepository.findWithIdentifiersById(proposedAccommodationEntity.caseId).orThrowNotFound("id" to proposedAccommodationEntity.id)
    val accommodationTypeEntity = accommodationTypeRepository.findByIdOrNull(proposedAccommodationEntity.accommodationTypeId)
      .orThrowNotFound("id" to proposedAccommodationEntity.accommodationTypeId)
    val accommodationStatusEntity = proposedAccommodationEntity.accommodationStatusId?.let {
      accommodationStatusRepository.findByIdOrNull(it).orThrowNotFound("id" to it)
    }
    return toAccommodationSummary(
      crn = case.latestCrn(),
      proposedAccommodationEntity = proposedAccommodationEntity,
      accommodationTypeEntity = accommodationTypeEntity,
      accommodationStatusEntity = accommodationStatusEntity,
    )
  }
}
