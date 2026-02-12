package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.proposedaccommodation

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.domain.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationTransformer
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class)
class ProposedAccommodationQueryServiceTest {

  @MockK
  lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  @InjectMockKs
  lateinit var service: ProposedAccommodationQueryService

  private val crn = "X12345"

  @Nested
  inner class GetProposedAccommodations {

    @Test
    fun `should return empty list when no accommodations exist`() {
      every { proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn) } returns emptyList()

      val result = service.getProposedAccommodations(crn)

      assertThat(result).isEmpty()
    }

    @Test
    fun `should preserve repository ordering`() {
      val olderDate = Instant.parse("2024-01-01T10:00:00Z")
      val middleDate = Instant.parse("2024-03-01T10:00:00Z")
      val newerDate = Instant.parse("2024-06-01T10:00:00Z")

      val entitiesInDbOrder = listOf(
        buildProposedAccommodationEntity(crn = crn, createdAt = newerDate),
        buildProposedAccommodationEntity(crn = crn, createdAt = middleDate),
        buildProposedAccommodationEntity(crn = crn, createdAt = olderDate),
      )

      every { proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn) } returns entitiesInDbOrder

      val result = service.getProposedAccommodations(crn)

      assertThat(result).hasSize(3)
      assertThat(result[0].createdAt).isEqualTo(newerDate)
      assertThat(result[1].createdAt).isEqualTo(middleDate)
      assertThat(result[2].createdAt).isEqualTo(olderDate)
    }

    @Test
    fun `should transform all entities correctly`() {
      val entities = listOf(
        buildProposedAccommodationEntity(crn = crn),
        buildProposedAccommodationEntity(crn = crn),
      )

      every { proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn) } returns entities

      val result = service.getProposedAccommodations(crn)

      assertThat(result).hasSize(2)
      val expectedDetails = ProposedAccommodationTransformer.toAccommodationDetails(entities)
      assertThat(result).containsExactlyInAnyOrderElementsOf(expectedDetails)
    }
  }

  @Nested
  inner class GetProposedAccommodation {

    private val id = UUID.randomUUID()

    @Test
    fun `should return accommodation when found by id and crn`() {
      val entity = buildProposedAccommodationEntity(crn = crn)

      every { proposedAccommodationRepository.findByIdAndCrn(id, crn) } returns entity

      val result = service.getProposedAccommodation(crn, id)

      assertThat(result.id).isEqualTo(entity.id)
      assertThat(result.name).isEqualTo(entity.name)
      assertThat(result.createdAt).isEqualTo(entity.createdAt)
    }

    @Test
    fun `should throw NotFoundException when not found`() {
      every { proposedAccommodationRepository.findByIdAndCrn(id, crn) } returns null

      assertThatThrownBy { service.getProposedAccommodation(crn, id) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessageContaining(id.toString())
    }
  }
}
