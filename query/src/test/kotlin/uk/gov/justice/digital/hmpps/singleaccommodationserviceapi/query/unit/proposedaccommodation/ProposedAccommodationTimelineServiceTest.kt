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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FieldChange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildFieldChange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.Username
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation.ProposedAccommodationTimelineService
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class)
class ProposedAccommodationTimelineServiceTest {

  @MockK
  lateinit var auditService: AuditService

  @MockK
  lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  @MockK
  lateinit var accommodationTypeRepository: AccommodationTypeRepository

  @MockK
  lateinit var userRepository: UserRepository

  @InjectMockKs
  lateinit var service: ProposedAccommodationTimelineService

  private val crn = UUID.randomUUID().toString()
  private val caseId = UUID.randomUUID()
  private val deliusSyncUser = buildUserEntity(
    username = "DELIUS_SYNC_USER",
    authSource = AuthSource.DELIUS,
    forename = "nDelius",
    surname = "user",
    email = "DELIUS_SYNC_USER",
  )

  @Nested
  inner class GetProposedAccommodationTimeline {

    @Test
    fun `should return proposed accommodation timeline when proposed accommodation record exists`() {
      val proposedAccommodationEntity = buildProposedAccommodationEntity(caseId = caseId)
      val auditEvent = buildAuditRecordDto()
      val accommodationType = buildAccommodationTypeEntity(
        code = "A07B",
        name = "Living in the home of a friend, family member or partner: settled",
      )
      every {
        userRepository.findByUsernameAndAuthSource(
          username = Username("DELIUS_SYNC_USER"),
          authSource = AuthSource.DELIUS,
        )
      } returns deliusSyncUser
      every {
        proposedAccommodationRepository.findByIdAndCrnWithNotes(
          eq(proposedAccommodationEntity.id),
          eq(crn),
        )
      } returns proposedAccommodationEntity
      every {
        auditService.fullAuditHistory(eq(proposedAccommodationEntity.id), eq(ProposedAccommodationEntity::class.java))
      } returns listOf(auditEvent)

      every { accommodationTypeRepository.findAll() } returns listOf(accommodationType)

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
      val accommodationType = buildAccommodationTypeEntity(
        code = "A07B",
        name = "Living in the home of a friend, family member or partner: settled",
      )
      val expectedProposedAccommodationCreatedAuditRecord = buildAuditRecordDto()

      every {
        userRepository.findByUsernameAndAuthSource(
          username = Username("DELIUS_SYNC_USER"),
          authSource = AuthSource.DELIUS,
        )
      } returns deliusSyncUser
      every {
        proposedAccommodationRepository.findByIdAndCrnWithNotes(
          eq(proposedAccommodationEntity.id),
          eq(crn),
        )
      } returns proposedAccommodationEntity
      every {
        auditService.fullAuditHistory(eq(proposedAccommodationEntity.id), eq(ProposedAccommodationEntity::class.java))
      } returns listOf(expectedProposedAccommodationCreatedAuditRecord)
      every { userRepository.findAllById(any()) } returns listOf(
        buildUserEntity(id = user1Id, forename = "Joe", surname = "Bloggs"),
        buildUserEntity(id = user2Id, forename = "Jane", surname = "Doe"),
      )
      every { accommodationTypeRepository.findAll() } returns listOf(accommodationType)

      val result = service.getProposedAccommodationTimeline(proposedAccommodationEntity.id, crn)

      assertThat(result).hasSize(3)
      assertThat(result.first()).isEqualTo(expectedProposedAccommodationCreatedAuditRecord)
      assertThat(result[1]).isEqualTo(
        buildAuditRecordDto(
          type = AuditRecordType.NOTE,
          author = "Joe Bloggs",
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
          author = "Jane Doe",
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
      every {
        userRepository.findByUsernameAndAuthSource(
          username = Username("DELIUS_SYNC_USER"),
          authSource = AuthSource.DELIUS,
        )
      } returns deliusSyncUser
      every { proposedAccommodationRepository.findByIdAndCrnWithNotes(id, crn) } returns null

      assertThatThrownBy { service.getProposedAccommodationTimeline(id, crn) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("ProposedAccommodationEntity not found for [id=$id, crn=$crn]")
    }

    @Test
    fun `should return proposed accommodation timeline and replace accommodationTypeId create field change with description`() {
      val crn = "X12345"
      val accommodationType = buildAccommodationTypeEntity(
        code = "A07B",
        name = "Living in the home of a friend, family member or partner: settled",
      )
      val proposedAccommodationEntity = buildProposedAccommodationEntity(
        caseId = caseId,
        accommodationTypeEntity = accommodationType,
      )
      val auditRecord = buildAuditRecordDto(
        type = AuditRecordType.CREATE,
        changes = listOf(
          FieldChange(
            field = "accommodationTypeId",
            value = accommodationType.id.toString(),
          ),
        ),
      )

      every {
        userRepository.findByUsernameAndAuthSource(
          username = Username("DELIUS_SYNC_USER"),
          authSource = AuthSource.DELIUS,
        )
      } returns deliusSyncUser
      every {
        proposedAccommodationRepository.findByIdAndCrnWithNotes(
          eq(proposedAccommodationEntity.id),
          eq(crn),
        )
      } returns proposedAccommodationEntity

      every {
        auditService.fullAuditHistory(proposedAccommodationEntity.id, ProposedAccommodationEntity::class.java)
      } returns listOf(auditRecord)

      every { accommodationTypeRepository.findAll() } returns listOf(accommodationType)

      val result = service.getProposedAccommodationTimeline(proposedAccommodationEntity.id, crn)

      val change = result.single().changes.single()

      assertThat(change.field).isEqualTo("accommodationTypeDescription")
      assertThat(change.value).isEqualTo("Living in the home of a friend, family member or partner: settled")
    }

    @Test
    fun `should return proposed accommodation timeline and replaces accommodationTypeId update field change with description values`() {
      val id = UUID.randomUUID()
      val oldType = buildAccommodationTypeEntity(
        code = "A07B",
        name = "Living in the home of a friend, family member or partner: settled",
      )
      val newType = buildAccommodationTypeEntity(
        code = "A01D",
        name = "Rental accommodation - social rental (LA or other)",
      )
      val proposedAccommodationEntity = buildProposedAccommodationEntity(
        id = id,
        accommodationTypeEntity = newType,
      )
      val auditRecord = buildAuditRecordDto(
        type = AuditRecordType.UPDATE,
        changes = listOf(
          FieldChange(
            field = "accommodationTypeId",
            value = newType.id.toString(),
            oldValue = oldType.id.toString(),
          ),
        ),
      )

      every {
        userRepository.findByUsernameAndAuthSource(
          username = Username("DELIUS_SYNC_USER"),
          authSource = AuthSource.DELIUS,
        )
      } returns deliusSyncUser
      every {
        proposedAccommodationRepository.findByIdAndCrnWithNotes(
          eq(proposedAccommodationEntity.id),
          eq(crn),
        )
      } returns proposedAccommodationEntity

      every {
        auditService.fullAuditHistory(id, ProposedAccommodationEntity::class.java)
      } returns listOf(auditRecord)

      every {
        accommodationTypeRepository.findAll()
      } returns listOf(oldType, newType)

      val result = service.getProposedAccommodationTimeline(id, crn)

      val change = result.single().changes.single()

      assertThat(change.field).isEqualTo("accommodationTypeDescription")
      assertThat(change.oldValue).isEqualTo("Living in the home of a friend, family member or partner: settled")
      assertThat(change.value).isEqualTo("Rental accommodation - social rental (LA or other)")
    }

    @Test
    fun `should return proposed accommodation timeline and does not modify unrelated audit fields`() {
      val id = UUID.randomUUID()
      val accommodationType = buildAccommodationTypeEntity()
      val proposedAccommodationEntity = buildProposedAccommodationEntity(
        id = id,
        accommodationTypeEntity = accommodationType,
      )
      val auditRecord = buildAuditRecordDto(
        type = AuditRecordType.UPDATE,
        changes = listOf(
          FieldChange(
            field = "nextAccommodationStatus",
            value = "YES",
            oldValue = "NO",
          ),
        ),
      )

      every {
        userRepository.findByUsernameAndAuthSource(
          username = Username("DELIUS_SYNC_USER"),
          authSource = AuthSource.DELIUS,
        )
      } returns deliusSyncUser
      every {
        proposedAccommodationRepository.findByIdAndCrnWithNotes(
          eq(proposedAccommodationEntity.id),
          eq(crn),
        )
      } returns proposedAccommodationEntity

      every {
        auditService.fullAuditHistory(id, ProposedAccommodationEntity::class.java)
      } returns listOf(auditRecord)

      every { accommodationTypeRepository.findAll() } returns listOf(accommodationType)

      val result = service.getProposedAccommodationTimeline(id, crn)

      val change = result.single().changes.single()

      assertThat(change.field).isEqualTo("nextAccommodationStatus")
      assertThat(change.oldValue).isEqualTo("NO")
      assertThat(change.value).isEqualTo("YES")
    }

    @Test
    fun `should nullify commit date for proposed accommodation audit records authored by Delius sync user`() {
      val id = UUID.randomUUID()
      val accommodationType = buildAccommodationTypeEntity()
      val proposedAccommodationEntity = buildProposedAccommodationEntity(
        id = id,
        accommodationTypeEntity = accommodationType,
      )
      val deliusSyncAuditRecord = buildAuditRecordDto(
        author = deliusSyncUser.displayName(),
        commitDate = Instant.now(),
      )
      val otherUserAuditRecord = buildAuditRecordDto(
        author = "Joe Bloggs",
        commitDate = Instant.now().minusSeconds(60),
      )

      every {
        userRepository.findByUsernameAndAuthSource(
          username = Username("DELIUS_SYNC_USER"),
          authSource = AuthSource.DELIUS,
        )
      } returns deliusSyncUser
      every {
        proposedAccommodationRepository.findByIdAndCrnWithNotes(
          eq(proposedAccommodationEntity.id),
          eq(crn),
        )
      } returns proposedAccommodationEntity
      every {
        auditService.fullAuditHistory(id, ProposedAccommodationEntity::class.java)
      } returns listOf(deliusSyncAuditRecord, otherUserAuditRecord)
      every { accommodationTypeRepository.findAll() } returns listOf(accommodationType)

      val result = service.getProposedAccommodationTimeline(id, crn)

      assertThat(result).hasSize(2)
      assertThat(result[0]).isEqualTo(
        deliusSyncAuditRecord.copy(
          commitDate = null,
        ),
      )
      assertThat(result[1]).isEqualTo(otherUserAuditRecord)
    }
  }
}
