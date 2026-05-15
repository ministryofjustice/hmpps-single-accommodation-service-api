package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.referencedata

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ReferenceDataDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationStatusRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto

@Service
class AccommodationStatusQueryService(
  private val accommodationStatusRepository: AccommodationStatusRepository,
) {
  fun getAccommodationStatuses(): ApiResponseDto<List<ReferenceDataDto>> = toApiResponseDto(
    data = accommodationStatusRepository.findAllByActiveIsTrueOrderByName()
      .map { ReferenceDataTransformer.toReferenceDataDto(it) },
  )
}
