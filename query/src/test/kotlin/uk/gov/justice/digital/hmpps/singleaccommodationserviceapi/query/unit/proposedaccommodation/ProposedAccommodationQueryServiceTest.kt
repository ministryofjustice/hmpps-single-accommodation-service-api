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
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationTransformer.toAccommodationDetail
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class)
class ProposedAccommodationQueryServiceTest {

  @MockK
  lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  @MockK
  lateinit var userRepository: UserRepository

  @MockK
  lateinit var accommodationTypeRepository: AccommodationTypeRepository

  @MockK
  lateinit var caseRepository: CaseRepository

  @InjectMockKs
  lateinit var service: ProposedAccommodationQueryService

  private val crn = UUID.randomUUID().toString()
  private val caseId = UUID.randomUUID()

  @Nested
  inner class GetProposedAccommodations {

    @Test
    fun `should return empty list when no accommodations exist`() {
      every { proposedAccommodationRepository.findAllProposedAccommodationByCrnOrderByCreatedAtDesc(crn) } returns emptyList()

      val result = service.getProposedAccommodations(crn)

      assertThat(result).isEmpty()
    }

    @Test
    fun `should preserve repository ordering`() {
      val createdByUserId = UUID.randomUUID()
      val createdByUser = buildUserEntity(id = createdByUserId)
      val olderDate = Instant.parse("2024-01-01T10:00:00Z")
      val middleDate = Instant.parse("2024-03-01T10:00:00Z")
      val newerDate = Instant.parse("2024-06-01T10:00:00Z")

      val accommodationTypeEntity = buildAccommodationTypeEntity()
      val entitiesInDbOrder = listOf(
        buildProposedAccommodationEntity(caseId = caseId, accommodationTypeEntity = accommodationTypeEntity, createdAt = newerDate, createdByUserId = createdByUserId),
        buildProposedAccommodationEntity(caseId = caseId, accommodationTypeEntity = accommodationTypeEntity, createdAt = middleDate, createdByUserId = createdByUserId),
        buildProposedAccommodationEntity(caseId = caseId, accommodationTypeEntity = accommodationTypeEntity, createdAt = olderDate, createdByUserId = createdByUserId),
      )

      every { proposedAccommodationRepository.findAllProposedAccommodationByCrnOrderByCreatedAtDesc(crn) } returns entitiesInDbOrder
      every { userRepository.findAllById(any()) } returns listOf(createdByUser)
      every { accommodationTypeRepository.findAllById(any()) } returns listOf(accommodationTypeEntity)

      val result = service.getProposedAccommodations(crn)

      assertThat(result).hasSize(3)
      assertThat(result[0].createdAt).isEqualTo(newerDate)
      assertThat(result[1].createdAt).isEqualTo(middleDate)
      assertThat(result[2].createdAt).isEqualTo(olderDate)
    }

    @Test
    fun `should transform all entities correctly`() {
      val createdByUserId = UUID.randomUUID()
      val createdByUser = buildUserEntity(id = createdByUserId)
      val caseId2 = UUID.randomUUID()
      val accommodationTypeEntity = buildAccommodationTypeEntity()
      val proposedAccommodationEntity1 =
        buildProposedAccommodationEntity(caseId = caseId, accommodationTypeEntity = accommodationTypeEntity, createdByUserId = createdByUserId, createdAt = Instant.now())
      val proposedAccommodationEntity2 =
        buildProposedAccommodationEntity(caseId = caseId2, accommodationTypeEntity = accommodationTypeEntity, createdByUserId = createdByUserId, createdAt = Instant.now())
      val entities = listOf(proposedAccommodationEntity1, proposedAccommodationEntity2)

      every { proposedAccommodationRepository.findAllProposedAccommodationByCrnOrderByCreatedAtDesc(crn) } returns entities
      every { userRepository.findAllById(any()) } returns listOf(createdByUser)
      every { accommodationTypeRepository.findAllById(any()) } returns listOf(accommodationTypeEntity)

      val result = service.getProposedAccommodations(crn)

      assertThat(result).hasSize(2)
      assertThat(result.first()).isEqualTo(toAccommodationDetail(proposedAccommodationEntity1, accommodationTypeEntity, crn, createdByUser.displayName()))
      assertThat(result[1]).isEqualTo(toAccommodationDetail(proposedAccommodationEntity2, accommodationTypeEntity, crn, createdByUser.displayName()))
    }
  }

  @Nested
  inner class GetProposedAccommodationByCrnAndId {

    private val id = UUID.randomUUID()

    @Test
    fun `should return accommodation when found by id and crn`() {
      val createdByUserId = UUID.randomUUID()
      val accommodationTypeEntity = buildAccommodationTypeEntity()
      val proposedAccommodationEntity = buildProposedAccommodationEntity(
        caseId = caseId,
        accommodationTypeEntity = accommodationTypeEntity,
        createdByUserId = createdByUserId,
      )
      val userEntity = buildUserEntity(id = createdByUserId)
      every { proposedAccommodationRepository.findByIdAndCrn(id, crn) } returns proposedAccommodationEntity
      every { userRepository.findByIdOrNull(createdByUserId) } returns userEntity
      every { accommodationTypeRepository.findByIdOrNull(accommodationTypeEntity.id) } returns accommodationTypeEntity

      val result = service.getProposedAccommodation(crn, id)

      assertThat(result.id).isEqualTo(proposedAccommodationEntity.id)
      assertThat(result.name).isEqualTo(proposedAccommodationEntity.name)
      assertThat(result.createdAt).isEqualTo(proposedAccommodationEntity.createdAt)
    }

    @Test
    fun `should throw NotFoundException when not found`() {
      every { proposedAccommodationRepository.findByIdAndCrn(id, crn) } returns null

      assertThatThrownBy { service.getProposedAccommodation(crn, id) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("ProposedAccommodationEntity not found for [id=$id, crn=$crn]")
    }
  }
}
