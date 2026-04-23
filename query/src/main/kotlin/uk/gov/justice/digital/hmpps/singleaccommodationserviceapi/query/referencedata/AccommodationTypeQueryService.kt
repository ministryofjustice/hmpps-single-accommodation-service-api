package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.referencedata

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ReferenceDataDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto

@Service
class AccommodationTypeQueryService(
  private val accommodationTypeRepository: AccommodationTypeRepository,
) {
  fun getAccommodationTypes(): ApiResponseDto<List<ReferenceDataDto>> = toApiResponseDto(
    data = accommodationTypeRepository.findAllByActiveIsTrueOrderByName()
      .map { ReferenceDataTransformer.toReferenceDataDto(it) },
  )
}
