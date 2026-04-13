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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildFieldChange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
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
  lateinit var auditService: AuditService

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
        buildProposedAccommodationEntity(caseId = caseId, createdAt = newerDate, createdByUserId = createdByUserId),
        buildProposedAccommodationEntity(caseId = caseId, createdAt = middleDate, createdByUserId = createdByUserId),
        buildProposedAccommodationEntity(caseId = caseId, createdAt = olderDate, createdByUserId = createdByUserId),
      )

      every { proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn) } returns entitiesInDbOrder
      every { userRepository.findByIdOrNull(createdByUserId) } returns createdByUser

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
      val caseId2 = UUID.randomUUID()
      val entity1 =
        buildProposedAccommodationEntity(caseId = caseId, createdByUserId = createdByUserId, createdAt = Instant.now())
      val entity2 =
        buildProposedAccommodationEntity(caseId = caseId2, createdByUserId = createdByUserId, createdAt = Instant.now())
      val entities = listOf(entity1, entity2)

      every { proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn) } returns entities
      every { userRepository.findByIdOrNull(createdByUserId) } returns createdByUser

      val result = service.getProposedAccommodations(crn)

      assertThat(result).hasSize(2)
      assertThat(result.first()).isEqualTo(toAccommodationDetail(entity1, crn, createdByUser.name))
      assertThat(result[1]).isEqualTo(toAccommodationDetail(entity2, crn, createdByUser.name))
    }
  }

  @Nested
  inner class GetProposedAccommodationByCrnAndId {

    private val id = UUID.randomUUID()

    @Test
    fun `should return accommodation when found by id and crn`() {
      val createdByUserId = UUID.randomUUID()
      val proposedAccommodationEntity = buildProposedAccommodationEntity(
        caseId = caseId,
        createdByUserId = createdByUserId,
      )
      val userEntity = buildUserEntity()
      every { proposedAccommodationRepository.findByIdAndCrn(id, crn) } returns proposedAccommodationEntity
      every { userRepository.findByIdOrNull(createdByUserId) } returns userEntity

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

  @Nested
  inner class GetProposedAccommodationById {

    private val id = UUID.randomUUID()

    @Test
    fun `should return accommodation when found by id`() {
      val createdByUserId = UUID.randomUUID()
      val proposedAccommodationEntity = buildProposedAccommodationEntity(
        id = id,
        caseId = caseId,
        createdByUserId = createdByUserId,
      )
      val userEntity = buildUserEntity()
      every { proposedAccommodationRepository.findByIdOrNull(id) } returns proposedAccommodationEntity
      every { userRepository.findByIdOrNull(createdByUserId) } returns userEntity
      every { caseRepository.findByIdOrNull(caseId) } returns buildCaseEntity()

      val result = service.getProposedAccommodation(id)

      assertThat(result.id).isEqualTo(proposedAccommodationEntity.id)
      assertThat(result.name).isEqualTo(proposedAccommodationEntity.name)
      assertThat(result.createdAt).isEqualTo(proposedAccommodationEntity.createdAt)
    }

    @Test
    fun `should throw NotFoundException when not found`() {
      every { proposedAccommodationRepository.findByIdOrNull(id) } returns null

      assertThatThrownBy { service.getProposedAccommodation(id) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("ProposedAccommodationEntity not found for [id=$id]")
    }
  }

  @Nested
  inner class GetProposedAccommodationTimeline {

    @Test
    fun `should return proposed accommodation timeline when proposed accommodation record exists`() {
      val proposedAccommodationEntity = buildProposedAccommodationEntity(caseId = caseId)
      val auditEvent = buildAuditRecordDto()

      every { proposedAccommodationRepository.findByIdAndCrnWithNotes(eq(proposedAccommodationEntity.id), eq(crn)) } returns proposedAccommodationEntity
      every {
        auditService.fullAuditHistory(eq(proposedAccommodationEntity.id), eq(ProposedAccommodationEntity::class.java))
      } returns listOf(auditEvent)

      val result = service.getProposedAccommodationTimeline(proposedAccommodationEntity.id, crn)

      assertThat(result).hasSize(1)
      assertThat(result.first()).isEqualTo(auditEvent)
    }

    @Test
    fun `should return proposed accommodation timeline when proposed accommodation record exists and related note exists`() {
      val user1Id = UUID.randomUUID()
      val user2Id = UUID.randomUUID()
      val proposedAccommodationEntity = buildProposedAccommodationEntity(
        caseId = caseId,
        createdByUserId = user1Id,
      )
      val noteCreatedAt = Instant.now().minusSeconds(10)
      val note2CreatedAt = Instant.now().minusSeconds(20)
      val note1Content = "1111"
      val note2Content = "1111"
      val notes = listOf(
        buildProposedAccommodationNoteEntity(
          id = UUID.randomUUID(),
          note = note1Content,
          createdByUserId = user1Id,
          createdAt = noteCreatedAt,
          proposedAccommodationEntity,
        ),
        buildProposedAccommodationNoteEntity(
          id = UUID.randomUUID(),
          note = note2Content,
          createdByUserId = user2Id,
          createdAt = note2CreatedAt,
          proposedAccommodationEntity,
        ),
      )
      proposedAccommodationEntity.apply {
        this.notes.addAll(notes)
      }
      val expectedProposedAccommodationCreatedAuditRecord = buildAuditRecordDto()
      val nameOfUser1 = "Joe Bloggs"
      val nameOfUser2 = "Jane Doe"
      every { proposedAccommodationRepository.findByIdAndCrnWithNotes(eq(proposedAccommodationEntity.id), eq(crn)) } returns proposedAccommodationEntity
      every {
        auditService.fullAuditHistory(eq(proposedAccommodationEntity.id), eq(ProposedAccommodationEntity::class.java))
      } returns listOf(expectedProposedAccommodationCreatedAuditRecord)
      every { userRepository.findAllById(any()) } returns listOf(buildUserEntity(id = user1Id, name = nameOfUser1), buildUserEntity(id = user2Id, name = nameOfUser2))

      val result = service.getProposedAccommodationTimeline(proposedAccommodationEntity.id, crn)

      assertThat(result).hasSize(3)
      assertThat(result.first()).isEqualTo(expectedProposedAccommodationCreatedAuditRecord)
      assertThat(result[1]).isEqualTo(
        buildAuditRecordDto(
          type = AuditRecordType.NOTE,
          author = nameOfUser1,
          commitDate = noteCreatedAt,
          changes = listOf(
            buildFieldChange(
              field = "note",
              value = note1Content,
            ),
          ),
        ),
      )
      assertThat(result[2]).isEqualTo(
        buildAuditRecordDto(
          type = AuditRecordType.NOTE,
          author = nameOfUser2,
          commitDate = note2CreatedAt,
          changes = listOf(
            buildFieldChange(
              field = "note",
              value = note2Content,
            ),
          ),
        ),
      )
    }

    @Test
    fun `should throw NotFoundException when not found`() {
      val id = UUID.randomUUID()
      every { proposedAccommodationRepository.findByIdAndCrnWithNotes(id, crn) } returns null

      assertThatThrownBy { service.getProposedAccommodationTimeline(id, crn) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("ProposedAccommodationEntity not found for [id=$id, crn=$crn]")
    }
  }
}
