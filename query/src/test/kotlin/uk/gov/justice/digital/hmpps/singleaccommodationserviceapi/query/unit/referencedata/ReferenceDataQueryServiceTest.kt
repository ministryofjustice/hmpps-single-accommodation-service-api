package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.referencedata

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildLocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.referencedata.LocalAuthorityAreaTransformer.toLocalAuthorityAreaDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.referencedata.ReferenceDataQueryService

@ExtendWith(MockKExtension::class)
class ReferenceDataQueryServiceTest {

  @MockK
  lateinit var localAuthorityAreaRepository: LocalAuthorityAreaRepository

  @InjectMockKs
  lateinit var service: ReferenceDataQueryService

  @Nested
  inner class GetLocalAuthorityAreas {

    @Test
    fun `should return all active local authority areas when no search provided`() {
      val entity = buildLocalAuthorityAreaEntity(identifier = "E09000001", name = "City of London")
      every { localAuthorityAreaRepository.findAllByActiveOrderByName(true) } returns listOf(entity)

      val result = service.getLocalAuthorityAreas(null, true)

      assertThat(result).hasSize(1)
      assertThat(result.first()).isEqualTo(toLocalAuthorityAreaDto(entity))
    }

    @Test
    fun `should delegate to search method when search parameter provided`() {
      val entity = buildLocalAuthorityAreaEntity(identifier = "E09000012", name = "London Borough of Hackney")
      every { localAuthorityAreaRepository.findAllByNameContainingIgnoreCaseAndActiveOrderByName("London", true) } returns listOf(entity)

      val result = service.getLocalAuthorityAreas("London", true)

      assertThat(result).hasSize(1)
      assertThat(result.first()).isEqualTo(toLocalAuthorityAreaDto(entity))
    }

    @Test
    fun `should pass active=false through to repository`() {
      val entity = buildLocalAuthorityAreaEntity(active = false)
      every { localAuthorityAreaRepository.findAllByActiveOrderByName(false) } returns listOf(entity)

      val result = service.getLocalAuthorityAreas(null, false)

      assertThat(result).hasSize(1)
      assertThat(result.first()).isEqualTo(toLocalAuthorityAreaDto(entity))
    }

    @Test
    fun `should return empty list when no results found`() {
      every { localAuthorityAreaRepository.findAllByNameContainingIgnoreCaseAndActiveOrderByName("NonExistent", true) } returns emptyList()

      val result = service.getLocalAuthorityAreas("NonExistent", true)

      assertThat(result).isEmpty()
    }
  }
}
