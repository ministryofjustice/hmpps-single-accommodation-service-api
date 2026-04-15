package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.referencedata

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ReferenceDataDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildLocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.referencedata.LocalAuthorityAreaQueryService

@ExtendWith(MockKExtension::class)
class LocalAuthorityAreaQueryServiceTest {

  @MockK
  lateinit var localAuthorityAreaRepository: LocalAuthorityAreaRepository

  @InjectMockKs
  lateinit var service: LocalAuthorityAreaQueryService

  @Test
  fun `should return all local authority areas ordered by name`() {
    val entity1 = buildLocalAuthorityAreaEntity(code = "E09000001", name = "City of London")
    val entity2 = buildLocalAuthorityAreaEntity(code = "E09000012", name = "London Borough of Hackney")
    every { localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName() } returns listOf(entity1, entity2)

    val result = service.getLocalAuthorityAreas()

    assertThat(result.data).hasSize(2)
    assertThat(result.data[0]).isEqualTo(ReferenceDataDto(id = entity1.id, name = "City of London", code = "E09000001"))
    assertThat(result.data[1]).isEqualTo(ReferenceDataDto(id = entity2.id, name = "London Borough of Hackney", code = "E09000012"))
  }
}
