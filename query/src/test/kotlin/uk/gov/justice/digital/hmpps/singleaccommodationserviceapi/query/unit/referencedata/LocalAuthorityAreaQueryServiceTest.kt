package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.referencedata

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildLocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.referencedata.LocalAuthorityAreaQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.referencedata.ReferenceDataTransformer

@ExtendWith(MockKExtension::class)
class LocalAuthorityAreaQueryServiceTest {

  @MockK
  lateinit var localAuthorityAreaRepository: LocalAuthorityAreaRepository

  @InjectMockKs
  lateinit var service: LocalAuthorityAreaQueryService

  @Test
  fun `should return all local authority areas ordered by name`() {
    val entity1 = buildLocalAuthorityAreaEntity(identifier = "E09000001", name = "City of London")
    val entity2 = buildLocalAuthorityAreaEntity(identifier = "E09000012", name = "London Borough of Hackney")
    every { localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName() } returns listOf(entity1, entity2)

    val result = service.getLocalAuthorityAreas()

    assertThat(result).hasSize(2)
    assertThat(result[0]).isEqualTo(ReferenceDataTransformer.toReferenceDataDto(entity1))
    assertThat(result[1]).isEqualTo(ReferenceDataTransformer.toReferenceDataDto(entity2))
  }
}
