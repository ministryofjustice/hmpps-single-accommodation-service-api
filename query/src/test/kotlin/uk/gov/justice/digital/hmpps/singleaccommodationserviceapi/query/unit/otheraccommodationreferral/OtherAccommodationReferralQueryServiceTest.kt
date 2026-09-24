package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.otheraccommodationreferral

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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FieldChange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOtherAccommodationReferralNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OtherAccommodationReferralRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.otheraccommodationreferral.OtherAccommodationReferralQueryService
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralStatus as EntityOtherAccommodationReferralStatus

@ExtendWith(MockKExtension::class)
class OtherAccommodationReferralQueryServiceTest {

  @MockK
  lateinit var otherAccommodationReferralRepository: OtherAccommodationReferralRepository

  @MockK
  lateinit var userRepository: UserRepository

  @MockK
  lateinit var auditService: AuditService

  @InjectMockKs
  lateinit var service: OtherAccommodationReferralQueryService

  private val caseId = UUID.randomUUID()
  private val id = UUID.randomUUID()
  private val crn = "X123456"
  private val createdByUserId = UUID.randomUUID()

  @Nested
  inner class GetOtherAccommodationReferral {

    @Test
    fun `should return other accommodation referral when found by crn and id`() {
      val entity = buildOtherAccommodationReferralEntity(
        id = id,
        caseId = caseId,
        crn = crn,
        createdByUserId = createdByUserId,
        submissionDate = LocalDate.of(2026, 2, 20),
        referenceNumber = "REF-001",
        organisationName = "Organisation name",
        website = "https://www.charity.org",
        submissionNote = "A submission note",
      )
      val userEntity = buildUserEntity(
        id = createdByUserId,
        forename = "Joe",
        surname = "Bloggs",
        username = "JBLOGGS",
      )

      every { otherAccommodationReferralRepository.findByIdAndCrn(id, crn) } returns entity
      every { userRepository.findByIdOrNull(createdByUserId) } returns userEntity

      val result = service.getOtherAccommodationReferral(crn, id)

      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.caseId).isEqualTo(caseId)
      assertThat(result.status).isEqualTo(OtherAccommodationReferralStatus.SUBMITTED)
      assertThat(result.submission).isNotNull
      val submission = result.submission
      assertThat(submission.id).isEqualTo(id)
      assertThat(submission.referenceNumber).isEqualTo("REF-001")
      assertThat(submission.submissionDate).isEqualTo(LocalDate.of(2026, 2, 20))
      assertThat(submission.createdBy).isEqualTo("Joe Bloggs")
      assertThat(submission.createdByUsername).isEqualTo("JBLOGGS")
      assertThat(submission.organisationName).isEqualTo("Organisation name")
      assertThat(submission.website).isEqualTo("https://www.charity.org")
      assertThat(submission.submissionNote).isEqualTo("A submission note")
    }

    @Test
    fun `should throw NotFoundException when not found`() {
      every { otherAccommodationReferralRepository.findByIdAndCrn(id, crn) } returns null

      assertThatThrownBy { service.getOtherAccommodationReferral(crn, id) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("OtherAccommodationReferralEntity not found for [id=$id, crn=$crn]")
    }
  }

  @Nested
  inner class SearchOtherAccommodationReferrals {

    @Test
    fun `should return empty list when no referrals found`() {
      every {
        otherAccommodationReferralRepository.searchByCrn(crn, null)
      } returns emptyList()

      val result = service.searchOtherAccommodationReferrals(crn, null)

      assertThat(result).isEmpty()
    }

    @Test
    fun `should filter by statuses and map to dto in descending submission date order`() {
      val userEntity = buildUserEntity(id = createdByUserId, forename = "Joe", surname = "Bloggs", username = "JBLOGGS")
      val newestEntity = buildOtherAccommodationReferralEntity(
        caseId = caseId,
        crn = crn,
        createdByUserId = createdByUserId,
        submissionDate = LocalDate.of(2026, 2, 1),
      )
      val oldestEntity = buildOtherAccommodationReferralEntity(
        caseId = caseId,
        crn = crn,
        createdByUserId = createdByUserId,
        submissionDate = LocalDate.of(2026, 1, 1),
      )

      every {
        otherAccommodationReferralRepository.searchByCrn(
          crn,
          listOf(EntityOtherAccommodationReferralStatus.SUBMITTED, EntityOtherAccommodationReferralStatus.ACCEPTED),
        )
      } returns listOf(newestEntity, oldestEntity)
      every { userRepository.findAllById(setOf(createdByUserId)) } returns listOf(userEntity)

      val result = service.searchOtherAccommodationReferrals(
        crn,
        listOf(OtherAccommodationReferralStatus.SUBMITTED, OtherAccommodationReferralStatus.ACCEPTED),
      )

      assertThat(result).hasSize(2)
      assertThat(result[0].submission.id).isEqualTo(newestEntity.id)
      assertThat(result[1].submission.id).isEqualTo(oldestEntity.id)
      assertThat(result[0].crn).isEqualTo(crn)
    }

    @Test
    fun `should treat an empty statuses list the same as no filter`() {
      every {
        otherAccommodationReferralRepository.searchByCrn(crn, null)
      } returns emptyList()

      val result = service.searchOtherAccommodationReferrals(crn, emptyList())

      assertThat(result).isEmpty()
    }
  }

  @Nested
  inner class GetOtherAccommodationReferralTimeline {

    @Test
    fun `should return empty list when there is no audit history and no notes`() {
      val otherAccommodationReferralEntity = buildOtherAccommodationReferralEntity(caseId = caseId, crn = crn)
      every { otherAccommodationReferralRepository.findByIdAndCrnWithNotes(otherAccommodationReferralEntity.id, crn) } returns otherAccommodationReferralEntity
      every { auditService.fullAuditHistory(otherAccommodationReferralEntity.id, OtherAccommodationReferralEntity::class.java) } returns emptyList()

      val result = service.getOtherAccommodationReferralTimeline(otherAccommodationReferralEntity.id, crn)

      assertThat(result.data).isEmpty()
    }

    @Test
    fun `should return audit history sorted by commit date descending`() {
      val otherAccommodationReferralEntity = buildOtherAccommodationReferralEntity(caseId = caseId, crn = crn)
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
          FieldChange(field = "referenceNumber", value = "OA-REF-002", oldValue = "OA-REF-001"),
        ),
      )

      every { otherAccommodationReferralRepository.findByIdAndCrnWithNotes(otherAccommodationReferralEntity.id, crn) } returns otherAccommodationReferralEntity
      every {
        auditService.fullAuditHistory(otherAccommodationReferralEntity.id, OtherAccommodationReferralEntity::class.java)
      } returns listOf(createRecord, updateRecord)

      val result = service.getOtherAccommodationReferralTimeline(otherAccommodationReferralEntity.id, crn)

      assertThat(result.data).hasSize(2)
      assertThat(result.data[0].commitDate).isEqualTo(updateRecord.commitDate)
      assertThat(result.data[1].commitDate).isEqualTo(createRecord.commitDate)
    }

    @Test
    fun `should merge multiple notes from different authors with audit history sorted descending`() {
      val user1Id = UUID.randomUUID()
      val user2Id = UUID.randomUUID()
      val otherAccommodationReferralEntity = buildOtherAccommodationReferralEntity(caseId = caseId, crn = crn)
      val note1CreatedAt = Instant.parse("2026-01-11T10:00:00Z")
      val note2CreatedAt = Instant.parse("2026-01-13T10:00:00Z")
      otherAccommodationReferralEntity.notes.add(
        buildOtherAccommodationReferralNoteEntity(
          note = "First note",
          createdByUserId = user1Id,
          createdAt = note1CreatedAt,
          otherAccommodationReferralEntity = otherAccommodationReferralEntity,
        ),
      )
      otherAccommodationReferralEntity.notes.add(
        buildOtherAccommodationReferralNoteEntity(
          note = "Second note",
          createdByUserId = user2Id,
          createdAt = note2CreatedAt,
          otherAccommodationReferralEntity = otherAccommodationReferralEntity,
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
          FieldChange(field = "referenceNumber", value = "OA-REF-002", oldValue = "OA-REF-001"),
        ),
      )
      val noteAuthor1 = buildUserEntity(id = user1Id, username = "user1", forename = "First", surname = "user")
      val noteAuthor2 = buildUserEntity(id = user2Id, username = "user2", forename = "Second", surname = "user")

      every { otherAccommodationReferralRepository.findByIdAndCrnWithNotes(otherAccommodationReferralEntity.id, crn) } returns otherAccommodationReferralEntity
      every {
        auditService.fullAuditHistory(otherAccommodationReferralEntity.id, OtherAccommodationReferralEntity::class.java)
      } returns listOf(createRecord, updateRecord)
      every { userRepository.findAllById(setOf(user1Id, user2Id)) } returns listOf(noteAuthor1, noteAuthor2)

      val result = service.getOtherAccommodationReferralTimeline(otherAccommodationReferralEntity.id, crn)

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
      every { otherAccommodationReferralRepository.findByIdAndCrnWithNotes(id, crn) } returns null

      assertThatThrownBy { service.getOtherAccommodationReferralTimeline(id, crn) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("OtherAccommodationReferralEntity not found for [id=$id, crn=$crn]")
    }
  }
}
