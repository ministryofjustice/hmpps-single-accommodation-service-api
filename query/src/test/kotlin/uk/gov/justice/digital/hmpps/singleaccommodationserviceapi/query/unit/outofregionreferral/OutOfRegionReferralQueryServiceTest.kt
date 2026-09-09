package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.outofregionreferral

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FieldChange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOutOfRegionReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOutOfRegionReferralNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutOfRegionReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutOfRegionReferralRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.outofregionreferral.OutOfRegionReferralQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.Instant
import java.time.LocalDate
import java.util.Optional
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OorStatus as EntityOorStatus

@ExtendWith(MockKExtension::class)
class OutOfRegionReferralQueryServiceTest {

  @MockK
  lateinit var outOfRegionReferralRepository: OutOfRegionReferralRepository

  @MockK
  lateinit var userRepository: UserRepository

  @MockK
  lateinit var caseRepository: CaseRepository

  @MockK
  lateinit var auditService: AuditService

  @InjectMockKs
  lateinit var service: OutOfRegionReferralQueryService

  private val caseId = UUID.randomUUID()
  private val crn = UUID.randomUUID().toString()
  private val clock = MutableClock()

  @Nested
  inner class GetOutOfRegionReferralByCrnAndCaseEntity {

    @Test
    fun `should return null when no OOR exists`() {
      val caseEntity = buildCaseEntity(id = caseId) { withCrn(crn) }
      every { outOfRegionReferralRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns null

      val result = service.getOutOfRegionReferral(caseEntity, crn)

      assertThat(result).isNull()
    }

    @Test
    fun `should return OOR with submission when OOR exists`() {
      val caseEntity = buildCaseEntity(id = caseId) { withCrn(crn) }
      val createdByUserId = UUID.randomUUID()
      val oorEntity = buildOutOfRegionReferralEntity(
        caseId = caseId,
        createdByUserId = createdByUserId,
      )
      val userEntity = buildUserEntity()
      every { outOfRegionReferralRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns oorEntity
      every { userRepository.findByIdOrNull(createdByUserId) } returns userEntity

      val result = service.getOutOfRegionReferral(caseEntity, crn)!!

      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.caseId).isEqualTo(caseId)
      assertThat(result.status).isEqualTo(OorStatus.SUBMITTED)
      assertThat(result.submission).isNotNull()
      val submission = result.submission!!
      assertThat(submission.createdBy).isEqualTo(userEntity.displayName())
    }
  }

  @Nested
  inner class GetOutOfRegionReferralHistory {

    @Test
    fun `should return empty list when no matching OORs exist`() {
      val caseEntity = buildCaseEntity(id = caseId) { withCrn(crn) }
      every { outOfRegionReferralRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns null
      every {
        outOfRegionReferralRepository.findByCaseIdAndStatusInOrderByCreatedAtDesc(caseId, any())
      } returns emptyList()

      assertThat(service.getOutOfRegionReferralHistory(caseEntity, crn)).isEmpty()
    }

    @Test
    fun `should query for accepted and not accepted statuses only and map the results`() {
      val caseEntity = buildCaseEntity(id = caseId) { withCrn(crn) }
      val createdByUserId = UUID.randomUUID()

      val acceptedOor = buildOutOfRegionReferralEntity(
        caseId = caseId,
        createdByUserId = createdByUserId,
        status = EntityOorStatus.ACCEPTED,
      )
      val userEntity = buildUserEntity(id = createdByUserId)

      val statusSlot = slot<List<EntityOorStatus>>()
      every { outOfRegionReferralRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns null
      every {
        outOfRegionReferralRepository.findByCaseIdAndStatusInOrderByCreatedAtDesc(caseId, capture(statusSlot))
      } returns listOf(acceptedOor)
      every { userRepository.findAllById(setOf(createdByUserId)) } returns listOf(userEntity)

      val result = service.getOutOfRegionReferralHistory(caseEntity, crn)

      assertThat(statusSlot.captured).containsExactlyInAnyOrder(
        EntityOorStatus.ACCEPTED,
        EntityOorStatus.NOT_ACCEPTED,
      )
      assertThat(result).hasSize(1)
      assertThat(result[0].status).isEqualTo(OorStatus.ACCEPTED)
      assertThat(result[0].crn).isEqualTo(crn)
    }

    @Test
    fun `should exclude the active referral from history`() {
      val caseEntity = buildCaseEntity(id = caseId) { withCrn(crn) }
      val historicUserId = UUID.randomUUID()

      val activeOor = buildOutOfRegionReferralEntity(
        id = UUID.randomUUID(),
        caseId = caseId,
        submissionDate = LocalDate.now().minusMonths(5),
        status = EntityOorStatus.ACCEPTED,
      )
      val historicOor = buildOutOfRegionReferralEntity(
        id = UUID.randomUUID(),
        caseId = caseId,
        createdByUserId = historicUserId,
        submissionDate = LocalDate.now().minusMonths(13),
        status = EntityOorStatus.NOT_ACCEPTED,
      )

      every { outOfRegionReferralRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns activeOor
      every {
        outOfRegionReferralRepository.findByCaseIdAndStatusInOrderByCreatedAtDesc(caseId, any())
      } returns listOf(activeOor, historicOor)
      every { userRepository.findAllById(setOf(historicUserId)) } returns listOf(buildUserEntity(id = historicUserId))

      val result = service.getOutOfRegionReferralHistory(caseEntity, crn)

      assertThat(result).hasSize(1)
      assertThat(result[0].submission!!.id).isEqualTo(historicOor.id)
      assertThat(result[0].status).isEqualTo(OorStatus.NOT_ACCEPTED)
    }

    @Test
    fun `should include the latest referral in history when it is expired`() {
      val caseEntity = buildCaseEntity(id = caseId) { withCrn(crn) }
      val userId = UUID.randomUUID()
      val expiredLatest = buildOutOfRegionReferralEntity(
        id = UUID.randomUUID(),
        caseId = caseId,
        createdByUserId = userId,
        submissionDate = LocalDate.now().minusMonths(7),
        status = EntityOorStatus.ACCEPTED,
      )

      every { outOfRegionReferralRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns expiredLatest
      every {
        outOfRegionReferralRepository.findByCaseIdAndStatusInOrderByCreatedAtDesc(caseId, any())
      } returns listOf(expiredLatest)
      every { userRepository.findAllById(setOf(userId)) } returns listOf(buildUserEntity(id = userId))

      val result = service.getOutOfRegionReferralHistory(caseEntity, crn)

      assertThat(result).hasSize(1)
      assertThat(result[0].submission!!.id).isEqualTo(expiredLatest.id)
    }
  }

  @Nested
  inner class GetOutOfRegionReferralByCrnAndId {

    private val id = UUID.randomUUID()

    @Test
    fun `should return oor when found by id and crn`() {
      val createdByUserId = UUID.randomUUID()
      val oorEntity = buildOutOfRegionReferralEntity(
        id = id,
        caseId = caseId,
        createdByUserId = createdByUserId,
        submissionDate = LocalDate.now().minusMonths(5),
      )
      val userEntity = buildUserEntity()

      every { outOfRegionReferralRepository.findByIdAndCrn(id, crn) } returns oorEntity
      every { outOfRegionReferralRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns oorEntity
      every { userRepository.findByIdOrNull(createdByUserId) } returns userEntity

      val result = service.getOutOfRegionReferral(crn, id)

      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.caseId).isEqualTo(caseId)
      assertThat(result.status).isEqualTo(OorStatus.SUBMITTED)
      assertThat(result.active).isTrue()
      assertThat(result.submission).isNotNull()
      val submission = result.submission!!
      assertThat(submission.id).isEqualTo(id)
      assertThat(submission.createdBy).isEqualTo(userEntity.displayName())
    }

    @Test
    fun `should return active false when the referral is expired`() {
      val oorEntity = buildOutOfRegionReferralEntity(
        id = id,
        caseId = caseId,
        createdByUserId = UUID.randomUUID(),
        submissionDate = LocalDate.now().minusMonths(7),
        status = EntityOorStatus.SUBMITTED,
      )

      every { outOfRegionReferralRepository.findByIdAndCrn(id, crn) } returns oorEntity
      every { outOfRegionReferralRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns oorEntity
      every { userRepository.findByIdOrNull(any()) } returns buildUserEntity()

      assertThat(service.getOutOfRegionReferral(crn, id).active).isFalse()
    }

    @Test
    fun `should return active false when a newer referral supersedes this one`() {
      val oorEntity = buildOutOfRegionReferralEntity(
        id = id,
        caseId = caseId,
        createdByUserId = UUID.randomUUID(),
        submissionDate = LocalDate.now().minusMonths(5),
        status = EntityOorStatus.SUBMITTED,
      )
      val newerOor = buildOutOfRegionReferralEntity(
        id = UUID.randomUUID(),
        caseId = caseId,
        submissionDate = LocalDate.now().minusMonths(4),
        status = EntityOorStatus.SUBMITTED,
      )

      every { outOfRegionReferralRepository.findByIdAndCrn(id, crn) } returns oorEntity
      every { outOfRegionReferralRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns newerOor
      every { userRepository.findByIdOrNull(any()) } returns buildUserEntity()

      assertThat(service.getOutOfRegionReferral(crn, id).active).isFalse()
    }

    @Test
    fun `should throw NotFoundException when not found`() {
      every { outOfRegionReferralRepository.findByIdAndCrn(id, crn) } returns null

      assertThatThrownBy { service.getOutOfRegionReferral(crn, id) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("OutOfRegionReferralEntity not found for [id=$id, crn=$crn]")
    }
  }

  @Nested
  inner class GetOutOfRegionReferralById {

    private val id = UUID.randomUUID()

    @Test
    fun `should return oor when found by id`() {
      val createdByUserId = UUID.randomUUID()
      val oorEntity = buildOutOfRegionReferralEntity(
        caseId = caseId,
        createdByUserId = createdByUserId,
      )
      val userEntity = buildUserEntity()
      every { outOfRegionReferralRepository.findById(id) } returns Optional.of(oorEntity)
      every { caseRepository.findWithIdentifiersById(caseId) } returns buildCaseEntity { withCrn(crn) }
      every { userRepository.findByIdOrNull(createdByUserId) } returns userEntity

      val result = service.getOutOfRegionReferral(id)

      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.caseId).isEqualTo(caseId)
      assertThat(result.status).isEqualTo(OorStatus.SUBMITTED)
      assertThat(result.submission).isNotNull()
      val submission = result.submission!!
      assertThat(submission.createdBy).isEqualTo(userEntity.displayName())
    }

    @Test
    fun `should throw NotFoundException when not found`() {
      every { outOfRegionReferralRepository.findByIdOrNull(id) } returns null

      assertThatThrownBy { service.getOutOfRegionReferral(id) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("OutOfRegionReferralEntity not found for [id=$id]")
    }
  }

  @Nested
  inner class GetOutOfRegionReferralTimeline {

    @Test
    fun `should return empty list when there is no audit history and no notes`() {
      val oorEntity = buildOutOfRegionReferralEntity(caseId = caseId)
      every { outOfRegionReferralRepository.findByIdAndCrnWithNotes(oorEntity.id, crn) } returns oorEntity
      every { auditService.fullAuditHistory(oorEntity.id, OutOfRegionReferralEntity::class.java) } returns emptyList()

      val result = service.getOutOfRegionReferralTimeline(oorEntity.id, crn)

      assertThat(result.data).isEmpty()
    }

    @Test
    fun `should merge multiple notes from different authors with audit history sorted descending`() {
      val user1Id = UUID.randomUUID()
      val user2Id = UUID.randomUUID()
      val oorEntity = buildOutOfRegionReferralEntity(caseId = caseId)
      val note1CreatedAt = Instant.parse("2026-01-11T10:00:00Z")
      val note2CreatedAt = Instant.parse("2026-01-13T10:00:00Z")
      oorEntity.notes.add(
        buildOutOfRegionReferralNoteEntity(
          note = "First note",
          createdByUserId = user1Id,
          createdAt = note1CreatedAt,
          outOfRegionReferralEntity = oorEntity,
        ),
      )
      oorEntity.notes.add(
        buildOutOfRegionReferralNoteEntity(
          note = "Second note",
          createdByUserId = user2Id,
          createdAt = note2CreatedAt,
          outOfRegionReferralEntity = oorEntity,
        ),
      )
      val createRecord = buildAuditRecordDto(
        type = AuditRecordType.CREATE,
        commitDate = Instant.parse("2026-01-10T10:00:00Z"),
        changes = listOf(
          FieldChange(field = "status", value = "SUBMITTED"),
        ),
      )
      val updateRecord = buildAuditRecordDto(
        type = AuditRecordType.UPDATE,
        commitDate = Instant.parse("2026-01-12T10:00:00Z"),
        changes = listOf(
          FieldChange(field = "status", value = "ACCEPTED", oldValue = "SUBMITTED"),
        ),
      )
      val noteAuthor1 = buildUserEntity(id = user1Id, username = "user1", forename = "First", surname = "user")
      val noteAuthor2 = buildUserEntity(id = user2Id, username = "user2", forename = "Second", surname = "user")

      every { outOfRegionReferralRepository.findByIdAndCrnWithNotes(oorEntity.id, crn) } returns oorEntity
      every {
        auditService.fullAuditHistory(oorEntity.id, OutOfRegionReferralEntity::class.java)
      } returns listOf(createRecord, updateRecord)
      every { userRepository.findAllById(setOf(user1Id, user2Id)) } returns listOf(noteAuthor1, noteAuthor2)

      val result = service.getOutOfRegionReferralTimeline(oorEntity.id, crn)

      assertThat(result.data).hasSize(4)
      assertThat(result.data[0].type).isEqualTo(AuditRecordType.NOTE)
      assertThat(result.data[0].commitDate).isEqualTo(note2CreatedAt)
      assertThat(result.data[0].author).isEqualTo("Second user")
      assertThat(result.data[0].authorDetails).isEqualTo(
        AssignedToDto(forename = "Second", surname = "user", username = "user2"),
      )
      assertThat(result.data[1].type).isEqualTo(AuditRecordType.UPDATE)
      assertThat(result.data[1].commitDate).isEqualTo(updateRecord.commitDate)
      assertThat(result.data[2].type).isEqualTo(AuditRecordType.NOTE)
      assertThat(result.data[2].commitDate).isEqualTo(note1CreatedAt)
      assertThat(result.data[2].author).isEqualTo("First user")
      assertThat(result.data[2].authorDetails).isEqualTo(
        AssignedToDto(forename = "First", surname = "user", username = "user1"),
      )
      assertThat(result.data[3].type).isEqualTo(AuditRecordType.CREATE)
      assertThat(result.data[3].commitDate).isEqualTo(createRecord.commitDate)
    }

    @Test
    fun `should throw NotFoundException when not found`() {
      val id = UUID.randomUUID()
      every { outOfRegionReferralRepository.findByIdAndCrnWithNotes(id, crn) } returns null

      assertThatThrownBy { service.getOutOfRegionReferralTimeline(id, crn) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("OutOfRegionReferralEntity not found for [id=$id, crn=$crn]")
    }
  }
}
