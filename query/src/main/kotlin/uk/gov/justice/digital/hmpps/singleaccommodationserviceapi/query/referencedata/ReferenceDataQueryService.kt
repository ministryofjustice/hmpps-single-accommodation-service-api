package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.referencedata

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LocalAuthorityAreaDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository

@Service
class ReferenceDataQueryService(
  private val localAuthorityAreaRepository: LocalAuthorityAreaRepository,
) {

  fun getLocalAuthorityAreas(search: String?, active: Boolean): List<LocalAuthorityAreaDto> {
    val entities = if (search != null) {
      localAuthorityAreaRepository.findAllByNameContainingIgnoreCaseAndActiveOrderByName(search, active)
    } else {
      localAuthorityAreaRepository.findAllByActiveOrderByName(active)
    }
    return entities.map { LocalAuthorityAreaTransformer.toLocalAuthorityAreaDto(it) }
  }
}
