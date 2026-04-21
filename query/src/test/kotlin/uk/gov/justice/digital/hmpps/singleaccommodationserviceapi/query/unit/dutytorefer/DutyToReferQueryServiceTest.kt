package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.dutytorefer

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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CreateFieldChangeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpdateFieldChangeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildLocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferAuditKeys.LOCAL_AUTHORITY_AREA_NAME
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import java.time.Instant
import java.util.Optional
import java.util.UUID

@ExtendWith(MockKExtension::class)
class DutyToReferQueryServiceTest {

  @MockK
  lateinit var dutyToReferRepository: DutyToReferRepository

  @MockK
  lateinit var userRepository: UserRepository

  @MockK
  lateinit var localAuthorityAreaRepository: LocalAuthorityAreaRepository

  @MockK
  lateinit var caseRepository: CaseRepository

  @MockK
  lateinit var auditService: AuditService

  @InjectMockKs
  lateinit var service: DutyToReferQueryService

  private val caseId = UUID.randomUUID()
  private val crn = UUID.randomUUID().toString()

  @Nested
  inner class GetDutyToRefer {

    @Test
    fun `should return NOT_STARTED with null submission when no DTR exists`() {
      val caseEntity = buildCaseEntity(id = caseId) { withCrn(crn) }
      every { caseRepository.findByCrn(crn) } returns caseEntity
      every { dutyToReferRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns null

      val result = service.getDutyToRefer(crn)

      assertThat(result.caseId).isEqualTo(caseId)
      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.status).isEqualTo(DtrStatus.NOT_STARTED)
      assertThat(result.submission).isNull()
    }

    @Test
    fun `should return DTR with submission and localAuthorityAreaName when DTR exists`() {
      val createdByUserId = UUID.randomUUID()
      val localAuthorityAreaId = UUID.randomUUID()
      val dtrEntity = buildDutyToReferEntity(
        caseId = caseId,
        localAuthorityAreaId = localAuthorityAreaId,
        createdByUserId = createdByUserId,
      )
      val userEntity = buildUserEntity()
      val localAuthorityAreaEntity = buildLocalAuthorityAreaEntity(
        id = localAuthorityAreaId,
        name = "Test Local Authority",
      )

      every { dutyToReferRepository.findByIdOrNull(caseId) } returns dtrEntity
      every { caseRepository.findByIdOrNull(caseId) } returns buildCaseEntity { withCrn(crn) }
      every { userRepository.findByIdOrNull(createdByUserId) } returns userEntity
      every { localAuthorityAreaRepository.findByIdOrNull(localAuthorityAreaId) } returns localAuthorityAreaEntity

      val result = service.getDutyToRefer(caseId)

      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.caseId).isEqualTo(caseId)
      assertThat(result.status).isEqualTo(DtrStatus.SUBMITTED)
      assertThat(result.submission).isNotNull()
      val submission = result.submission!!
      assertThat(submission.localAuthority.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
      assertThat(submission.localAuthority.localAuthorityAreaName).isEqualTo(localAuthorityAreaEntity.name)
      assertThat(submission.createdBy).isEqualTo(userEntity.name)
    }
  }

  @Nested
  inner class GetDutyToReferByCrnAndId {

    private val id = UUID.randomUUID()

    @Test
    fun `should return dtr when found by id and crn`() {
      val createdByUserId = UUID.randomUUID()
      val localAuthorityAreaId = UUID.randomUUID()
      val dtrEntity = buildDutyToReferEntity(
        id = id,
        caseId = caseId,
        localAuthorityAreaId = localAuthorityAreaId,
        createdByUserId = createdByUserId,
      )
      val userEntity = buildUserEntity()
      val localAuthorityAreaEntity = buildLocalAuthorityAreaEntity(
        id = localAuthorityAreaId,
        name = "Test Local Authority",
      )

      every { dutyToReferRepository.findByIdAndCrn(id, crn) } returns dtrEntity
      every { userRepository.findByIdOrNull(createdByUserId) } returns userEntity
      every { localAuthorityAreaRepository.findByIdOrNull(localAuthorityAreaId) } returns localAuthorityAreaEntity

      val result = service.getDutyToRefer(crn, id)

      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.caseId).isEqualTo(caseId)
      assertThat(result.status).isEqualTo(DtrStatus.SUBMITTED)
      assertThat(result.submission).isNotNull()
      val submission = result.submission!!
      assertThat(submission.id).isEqualTo(id)
      assertThat(submission.localAuthority.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
      assertThat(submission.localAuthority.localAuthorityAreaName).isEqualTo(localAuthorityAreaEntity.name)
      assertThat(submission.createdBy).isEqualTo(userEntity.name)
    }

    @Test
    fun `should throw NotFoundException when not found`() {
      every { dutyToReferRepository.findByIdAndCrn(id, crn) } returns null

      assertThatThrownBy { service.getDutyToRefer(crn, id) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("DutyToReferEntity not found for [id=$id, crn=$crn]")
    }
  }

  @Nested
  inner class GetDutyToReferById {

    private val id = UUID.randomUUID()

    @Test
    fun `should return dtr when found by id`() {
      val createdByUserId = UUID.randomUUID()
      val localAuthorityAreaId = UUID.randomUUID()
      val dtrEntity = buildDutyToReferEntity(
        caseId = caseId,
        localAuthorityAreaId = localAuthorityAreaId,
        createdByUserId = createdByUserId,
      )
      val userEntity = buildUserEntity()
      val localAuthorityAreaEntity = buildLocalAuthorityAreaEntity(
        id = localAuthorityAreaId,
        name = "Test Local Authority",
      )
      every { dutyToReferRepository.findById(id) } returns Optional.of(dtrEntity)
      every { caseRepository.findByIdOrNull(caseId) } returns buildCaseEntity { withCrn(crn) }
      every { userRepository.findByIdOrNull(createdByUserId) } returns userEntity
      every { localAuthorityAreaRepository.findByIdOrNull(localAuthorityAreaId) } returns localAuthorityAreaEntity

      val result = service.getDutyToRefer(id)

      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.caseId).isEqualTo(caseId)
      assertThat(result.status).isEqualTo(DtrStatus.SUBMITTED)
      assertThat(result.submission).isNotNull()
      val submission = result.submission!!
      assertThat(submission.localAuthority.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
      assertThat(submission.localAuthority.localAuthorityAreaName).isEqualTo(localAuthorityAreaEntity.name)
      assertThat(submission.createdBy).isEqualTo(userEntity.name)
    }

    @Test
    fun `should throw NotFoundException when not found`() {
      every { dutyToReferRepository.findByIdOrNull(id) } returns null

      assertThatThrownBy { service.getDutyToRefer(id) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("DutyToReferEntity not found for [id=$id]")
    }
  }

  @Nested
  inner class GetDutyToReferTimeline {

    @Test
    fun `should return empty list when there is no audit history and no notes`() {
      val dtrEntity = buildDutyToReferEntity(caseId = caseId)
      every { dutyToReferRepository.findByIdAndCrnWithNotes(dtrEntity.id, crn) } returns dtrEntity
      every { auditService.fullAuditHistory(dtrEntity.id, DutyToReferEntity::class.java) } returns emptyList()
      every { localAuthorityAreaRepository.findAllById(emptySet()) } returns emptyList()

      val result = service.getDutyToReferTimeline(dtrEntity.id, crn)

      assertThat(result.data).isEmpty()
    }

    @Test
    fun `should populate localAuthorityAreaName on events that do not change the local authority`() {
      val localAuthorityAreaId = UUID.randomUUID()
      val dtrEntity = buildDutyToReferEntity(caseId = caseId, localAuthorityAreaId = localAuthorityAreaId)
      val laEntity = buildLocalAuthorityAreaEntity(id = localAuthorityAreaId, name = "Cherwell")
      val createRecord = buildAuditRecordDto(
        type = AuditRecordType.CREATE,
        commitDate = Instant.parse("2026-01-10T10:00:00Z"),
        changes = listOf(
          CreateFieldChangeDto(field = "localAuthorityAreaId", value = localAuthorityAreaId.toString()),
          CreateFieldChangeDto(field = "status", value = "SUBMITTED"),
        ),
      )
      val updateRecord = buildAuditRecordDto(
        type = AuditRecordType.UPDATE,
        commitDate = Instant.parse("2026-01-12T10:00:00Z"),
        changes = listOf(
          UpdateFieldChangeDto(field = "status", value = "ACCEPTED", oldValue = "SUBMITTED"),
        ),
      )

      every { dutyToReferRepository.findByIdAndCrnWithNotes(dtrEntity.id, crn) } returns dtrEntity
      every {
        auditService.fullAuditHistory(dtrEntity.id, DutyToReferEntity::class.java)
      } returns listOf(createRecord, updateRecord)
      every { localAuthorityAreaRepository.findAllById(setOf(localAuthorityAreaId)) } returns listOf(laEntity)

      val result = service.getDutyToReferTimeline(dtrEntity.id, crn)

      assertThat(result.data).hasSize(2)
      assertThat(result.data[0].commitDate).isEqualTo(updateRecord.commitDate)
      assertThat(result.data[0].extraInformation[LOCAL_AUTHORITY_AREA_NAME]).isEqualTo("Cherwell")
      assertThat(result.data[1].commitDate).isEqualTo(createRecord.commitDate)
      assertThat(result.data[1].extraInformation[LOCAL_AUTHORITY_AREA_NAME]).isEqualTo("Cherwell")
    }

    @Test
    fun `should resolve local authority names correctly when local authority is changed`() {
      val initialLaId = UUID.randomUUID()
      val updatedLaId = UUID.randomUUID()
      val dtrEntity = buildDutyToReferEntity(caseId = caseId, localAuthorityAreaId = updatedLaId)
      val initialLa = buildLocalAuthorityAreaEntity(id = initialLaId, name = "Cherwell")
      val updatedLa = buildLocalAuthorityAreaEntity(id = updatedLaId, name = "Oxford")

      val createRecord = buildAuditRecordDto(
        type = AuditRecordType.CREATE,
        commitDate = Instant.parse("2026-01-10T10:00:00Z"),
        changes = listOf(
          CreateFieldChangeDto(field = "localAuthorityAreaId", value = initialLaId.toString()),
          CreateFieldChangeDto(field = "status", value = "SUBMITTED"),
        ),
      )
      val laChangeRecord = buildAuditRecordDto(
        type = AuditRecordType.UPDATE,
        commitDate = Instant.parse("2026-01-12T10:00:00Z"),
        changes = listOf(
          UpdateFieldChangeDto(
            field = "localAuthorityAreaId",
            value = updatedLaId.toString(),
            oldValue = initialLaId.toString(),
          ),
        ),
      )

      every { dutyToReferRepository.findByIdAndCrnWithNotes(dtrEntity.id, crn) } returns dtrEntity
      every {
        auditService.fullAuditHistory(dtrEntity.id, DutyToReferEntity::class.java)
      } returns listOf(createRecord, laChangeRecord)
      every {
        localAuthorityAreaRepository.findAllById(setOf(initialLaId, updatedLaId))
      } returns listOf(initialLa, updatedLa)

      val result = service.getDutyToReferTimeline(dtrEntity.id, crn)

      assertThat(result.data).hasSize(2)
      assertThat(result.data[0].commitDate).isEqualTo(laChangeRecord.commitDate)
      assertThat(result.data[0].extraInformation[LOCAL_AUTHORITY_AREA_NAME]).isEqualTo("Oxford")
      assertThat(result.data[1].commitDate).isEqualTo(createRecord.commitDate)
      assertThat(result.data[1].extraInformation[LOCAL_AUTHORITY_AREA_NAME]).isEqualTo("Cherwell")
    }

    @Test
    fun `should merge multiple notes from different authors with audit history sorted descending`() {
      val localAuthorityAreaId = UUID.randomUUID()
      val user1Id = UUID.randomUUID()
      val user2Id = UUID.randomUUID()
      val dtrEntity = buildDutyToReferEntity(caseId = caseId, localAuthorityAreaId = localAuthorityAreaId)
      val note1CreatedAt = Instant.parse("2026-01-11T10:00:00Z")
      val note2CreatedAt = Instant.parse("2026-01-13T10:00:00Z")
      dtrEntity.notes.add(
        buildDutyToReferNoteEntity(
          note = "First note",
          createdByUserId = user1Id,
          createdAt = note1CreatedAt,
          dutyToReferEntity = dtrEntity,
        ),
      )
      dtrEntity.notes.add(
        buildDutyToReferNoteEntity(
          note = "Second note",
          createdByUserId = user2Id,
          createdAt = note2CreatedAt,
          dutyToReferEntity = dtrEntity,
        ),
      )
      val laEntity = buildLocalAuthorityAreaEntity(id = localAuthorityAreaId, name = "Cherwell")
      val createRecord = buildAuditRecordDto(
        type = AuditRecordType.CREATE,
        commitDate = Instant.parse("2026-01-10T10:00:00Z"),
        changes = listOf(
          CreateFieldChangeDto(field = "localAuthorityAreaId", value = localAuthorityAreaId.toString()),
        ),
      )
      val updateRecord = buildAuditRecordDto(
        type = AuditRecordType.UPDATE,
        commitDate = Instant.parse("2026-01-12T10:00:00Z"),
        changes = listOf(
          UpdateFieldChangeDto(field = "status", value = "ACCEPTED", oldValue = "SUBMITTED"),
        ),
      )
      val noteAuthor1 = buildUserEntity(id = user1Id, name = "First user")
      val noteAuthor2 = buildUserEntity(id = user2Id, name = "Second user")

      every { dutyToReferRepository.findByIdAndCrnWithNotes(dtrEntity.id, crn) } returns dtrEntity
      every {
        auditService.fullAuditHistory(dtrEntity.id, DutyToReferEntity::class.java)
      } returns listOf(createRecord, updateRecord)
      every { localAuthorityAreaRepository.findAllById(setOf(localAuthorityAreaId)) } returns listOf(laEntity)
      every { userRepository.findAllById(setOf(user1Id, user2Id)) } returns listOf(noteAuthor1, noteAuthor2)

      val result = service.getDutyToReferTimeline(dtrEntity.id, crn)

      assertThat(result.data).hasSize(4)
      assertThat(result.data[0].type).isEqualTo(AuditRecordType.NOTE)
      assertThat(result.data[0].commitDate).isEqualTo(note2CreatedAt)
      assertThat(result.data[0].author).isEqualTo("Second user")
      assertThat(result.data[0].extraInformation[LOCAL_AUTHORITY_AREA_NAME]).isNull()
      assertThat(result.data[1].type).isEqualTo(AuditRecordType.UPDATE)
      assertThat(result.data[1].commitDate).isEqualTo(updateRecord.commitDate)
      assertThat(result.data[1].extraInformation[LOCAL_AUTHORITY_AREA_NAME]).isEqualTo("Cherwell")
      assertThat(result.data[2].type).isEqualTo(AuditRecordType.NOTE)
      assertThat(result.data[2].commitDate).isEqualTo(note1CreatedAt)
      assertThat(result.data[2].author).isEqualTo("First user")
      assertThat(result.data[2].extraInformation[LOCAL_AUTHORITY_AREA_NAME]).isNull()
      assertThat(result.data[3].type).isEqualTo(AuditRecordType.CREATE)
      assertThat(result.data[3].commitDate).isEqualTo(createRecord.commitDate)
      assertThat(result.data[3].extraInformation[LOCAL_AUTHORITY_AREA_NAME]).isEqualTo("Cherwell")
    }

    @Test
    fun `should throw NotFoundException when not found`() {
      val id = UUID.randomUUID()
      every { dutyToReferRepository.findByIdAndCrnWithNotes(id, crn) } returns null

      assertThatThrownBy { service.getDutyToReferTimeline(id, crn) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("DutyToReferEntity not found for [id=$id, crn=$crn]")
    }
  }
}
