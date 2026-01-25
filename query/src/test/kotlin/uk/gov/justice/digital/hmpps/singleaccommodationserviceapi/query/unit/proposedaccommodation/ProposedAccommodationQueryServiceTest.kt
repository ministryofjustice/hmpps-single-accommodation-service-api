package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.proposedaccommodation

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationTransformer.toAccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationTransformer.toAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationTransformer.toArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationTransformer.toArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationTransformer.toNextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationTransformer.toOffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationTransformer.toSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationTransformer.toVerificationStatus
import java.time.Instant
import java.util.Optional
import java.util.UUID

@ExtendWith(MockKExtension::class)
class ProposedAccommodationQueryServiceTest {

  @MockK
  lateinit var proposedAccommodationRepository: ProposedAccommodationRepository
  @MockK
  lateinit var userRepository: UserRepository

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
      val createdByUserId = UUID.randomUUID()
      val createdByUser = buildUserEntity()
      val olderDate = Instant.parse("2024-01-01T10:00:00Z")
      val middleDate = Instant.parse("2024-03-01T10:00:00Z")
      val newerDate = Instant.parse("2024-06-01T10:00:00Z")

      val entitiesInDbOrder = listOf(
        buildProposedAccommodationEntity(crn = crn, createdAt = newerDate, createdByUserId = createdByUserId),
        buildProposedAccommodationEntity(crn = crn, createdAt = middleDate, createdByUserId = createdByUserId),
        buildProposedAccommodationEntity(crn = crn, createdAt = olderDate, createdByUserId = createdByUserId),
      )

      every { proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn) } returns entitiesInDbOrder
      every { userRepository.findById(createdByUserId) } returns Optional.of(createdByUser)

      val result = service.getProposedAccommodations(crn)

      assertThat(result).hasSize(3)
      assertThat(result[0].createdAt).isEqualTo(newerDate)
      assertThat(result[1].createdAt).isEqualTo(middleDate)
      assertThat(result[2].createdAt).isEqualTo(olderDate)
    }

    @Test
    fun `should transform all entities correctly`() {
      val createdByUserId = UUID.randomUUID()
      val createdByUser = buildUserEntity()
      val entity1 = buildProposedAccommodationEntity(crn = crn, createdByUserId = createdByUserId)
      val entity2 = buildProposedAccommodationEntity(crn = crn, createdByUserId = createdByUserId)
      val entities = listOf(entity1, entity2)

      every { proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn) } returns entities
      every { userRepository.findById(createdByUserId) } returns Optional.of(createdByUser)

      val result = service.getProposedAccommodations(crn)

      assertThat(result).hasSize(2)
      assertThat(result.first()).isEqualTo(toAccommodationDetail(entity1, createdByUser.name))
      assertThat(result[1]).isEqualTo(toAccommodationDetail(entity2, createdByUser.name))
    }
  }
}
