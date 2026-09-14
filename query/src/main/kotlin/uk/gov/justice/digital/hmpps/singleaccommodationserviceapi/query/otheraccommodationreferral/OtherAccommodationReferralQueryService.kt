package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.otheraccommodationreferral

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OtherAccommodationReferralRepository
import java.util.UUID

@Service
@Transactional(readOnly = true)
class OtherAccommodationReferralQueryService(
  private val otherAccommodationReferralRepository: OtherAccommodationReferralRepository,
) {

  fun getOtherAccommodationReferral(crn: String, id: UUID): OtherAccommodationReferralDto {
    val entity = otherAccommodationReferralRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)

    return OtherAccommodationReferralTransformer.toOtherAccommodationReferralDto(
      entity = entity,
      crn = crn,
    )
  }
}
