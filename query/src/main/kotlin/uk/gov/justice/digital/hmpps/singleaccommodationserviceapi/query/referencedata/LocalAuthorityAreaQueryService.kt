package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.referencedata

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ReferenceDataDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository

@Service
class LocalAuthorityAreaQueryService(
  private val localAuthorityAreaRepository: LocalAuthorityAreaRepository,
) {

  fun getLocalAuthorityAreas(): List<ReferenceDataDto> = localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName()
    .map { ReferenceDataTransformer.toReferenceDataDto(it) }
}
