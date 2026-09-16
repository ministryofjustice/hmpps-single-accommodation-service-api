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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildLocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOtherAccommodationReferralNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OtherAccommodationReferralRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.otheraccommodationreferral.OtherAccommodationReferralQueryService
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockKExtension::class)
class OtherAccommodationReferralQueryServiceTest {

  @MockK
  lateinit var otherAccommodationReferralRepository: OtherAccommodationReferralRepository

  @MockK
  lateinit var userRepository: UserRepository

  @MockK
  lateinit var localAuthorityAreaRepository: LocalAuthorityAreaRepository

  @MockK
  lateinit var auditService: AuditService

  @InjectMockKs
  lateinit var service: OtherAccommodationReferralQueryService

  private val caseId = UUID.randomUUID()
  private val id = UUID.randomUUID()
  private val crn = "X123456"
  private val localAuthorityAreaId = UUID.randomUUID()
  private val createdByUserId = UUID.randomUUID()

  @Nested
  inner class GetOtherAccommodationReferral {

    @Test
    fun `should return other accommodation referral when found by crn and id`() {
      val entity = buildOtherAccommodationReferralEntity(
        id = id,
        caseId = caseId,
        crn = crn,
        localAuthorityAreaId = localAuthorityAreaId,
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
      val localAuthorityAreaEntity = buildLocalAuthorityAreaEntity(
        id = localAuthorityAreaId,
        name = "Test Local Authority",
      )

      every { otherAccommodationReferralRepository.findByIdAndCrn(id, crn) } returns entity
      every { userRepository.findByIdOrNull(createdByUserId) } returns userEntity
      every { localAuthorityAreaRepository.findByIdOrNull(localAuthorityAreaId) } returns localAuthorityAreaEntity

      val result = service.getOtherAccommodationReferral(crn, id)

      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.caseId).isEqualTo(caseId)
      assertThat(result.status).isEqualTo(OtherAccommodationReferralStatus.SUBMITTED)
      assertThat(result.submission).isNotNull
      val submission = result.submission
      assertThat(submission.id).isEqualTo(id)
      assertThat(submission.localAuthority.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
      assertThat(submission.localAuthority.localAuthorityAreaName).isEqualTo("Test Local Authority")
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
  private val crn = UUID.randomUUID().toString()

  @Nested
  inner class GetOtherAccommodationReferralTimeline {

    @Test
    fun `should return empty list when there is no audit history and no notes`() {
      val otherAccommodationReferralEntity = buildOtherAccommodationReferralEntity(caseId = caseId, crn = crn)
      every { otherAccommodationReferralRepository.findByIdAndCrnWithNotes(otherAccommodationReferralEntity.id, crn) } returns otherAccommodationReferralEntity
      every { auditService.fullAuditHistory(otherAccommodationReferralEntity.id, OtherAccommodationReferralEntity::class.java) } returns emptyList()
      every { localAuthorityAreaRepository.findAllById(emptySet()) } returns emptyList()

      val result = service.getOtherAccommodationReferralTimeline(otherAccommodationReferralEntity.id, crn)

      assertThat(result.data).isEmpty()
    }

    @Test
    fun `should populate localAuthorityAreaName on events that do not change the local authority`() {
      val localAuthorityAreaId = UUID.randomUUID()
      val otherAccommodationReferralEntity = buildOtherAccommodationReferralEntity(caseId = caseId, crn = crn, localAuthorityAreaId = localAuthorityAreaId)
      val laEntity = buildLocalAuthorityAreaEntity(id = localAuthorityAreaId, name = "Cherwell")
      val createRecord = buildAuditRecordDto(
        type = AuditRecordType.CREATE,
        commitDate = Instant.parse("2026-01-10T10:00:00Z"),
        changes = listOf(
          FieldChange(field = "localAuthorityAreaId", value = localAuthorityAreaId.toString()),
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
      every { localAuthorityAreaRepository.findAllById(setOf(localAuthorityAreaId)) } returns listOf(laEntity)

      val result = service.getOtherAccommodationReferralTimeline(otherAccommodationReferralEntity.id, crn)

      assertThat(result.data).hasSize(2)
      assertThat(result.data[0].commitDate).isEqualTo(updateRecord.commitDate)
      assertThat(result.data[0].extraInformation?.get("localAuthorityAreaName")).isEqualTo("Cherwell")
      assertThat(result.data[1].commitDate).isEqualTo(createRecord.commitDate)
      assertThat(result.data[1].extraInformation?.get("localAuthorityAreaName")).isEqualTo("Cherwell")
    }

    @Test
    fun `should resolve local authority names correctly when local authority is changed`() {
      val initialLaId = UUID.randomUUID()
      val updatedLaId = UUID.randomUUID()
      val otherAccommodationReferralEntity = buildOtherAccommodationReferralEntity(caseId = caseId, crn = crn, localAuthorityAreaId = updatedLaId)
      val initialLa = buildLocalAuthorityAreaEntity(id = initialLaId, name = "Cherwell")
      val updatedLa = buildLocalAuthorityAreaEntity(id = updatedLaId, name = "Oxford")

      val createRecord = buildAuditRecordDto(
        type = AuditRecordType.CREATE,
        commitDate = Instant.parse("2026-01-10T10:00:00Z"),
        changes = listOf(
          FieldChange(field = "localAuthorityAreaId", value = initialLaId.toString()),
          FieldChange(field = "status", value = "SUBMITTED"),
        ),
      )
      val laChangeRecord = laChange(
        from = initialLa.id,
        to = updatedLa.id,
        at = "2026-01-12T10:00:00Z",
      )

      every { otherAccommodationReferralRepository.findByIdAndCrnWithNotes(otherAccommodationReferralEntity.id, crn) } returns otherAccommodationReferralEntity
      every {
        auditService.fullAuditHistory(otherAccommodationReferralEntity.id, OtherAccommodationReferralEntity::class.java)
      } returns listOf(createRecord, laChangeRecord)
      every {
        localAuthorityAreaRepository.findAllById(setOf(initialLaId, updatedLaId))
      } returns listOf(initialLa, updatedLa)

      val result = service.getOtherAccommodationReferralTimeline(otherAccommodationReferralEntity.id, crn)

      assertThat(result.data).hasSize(2)
      assertThat(result.data[0].commitDate).isEqualTo(laChangeRecord.commitDate)
      assertThat(result.data[0].extraInformation?.get("localAuthorityAreaName")).isEqualTo("Oxford")
      assertThat(result.data[1].commitDate).isEqualTo(createRecord.commitDate)
      assertThat(result.data[1].extraInformation?.get("localAuthorityAreaName")).isEqualTo("Cherwell")
    }

    @Test
    fun `should show correct local authority for in between updates when LA does not change and when LA is then updated later`() {
      val initialLa = buildLocalAuthorityAreaEntity(id = UUID.randomUUID(), name = "Cherwell")
      val firstLa = buildLocalAuthorityAreaEntity(id = UUID.randomUUID(), name = "Oxford")
      val secondLa = buildLocalAuthorityAreaEntity(id = UUID.randomUUID(), name = "Gloucester")
      val thirdLa = buildLocalAuthorityAreaEntity(id = UUID.randomUUID(), name = "Cambridge")
      val fourthLa = buildLocalAuthorityAreaEntity(id = UUID.randomUUID(), name = "Stroud")

      val initialReference = UUID.randomUUID().toString()
      val firstRef = UUID.randomUUID().toString()
      val secondRef = UUID.randomUUID().toString()
      val thirdRef = UUID.randomUUID().toString()
      val fourthRef = UUID.randomUUID().toString()

      val otherAccommodationReferralEntity = buildOtherAccommodationReferralEntity(
        caseId = caseId,
        crn = crn,
        localAuthorityAreaId = fourthLa.id,
        referenceNumber = fourthRef,
      )
      val createRecord = buildAuditRecordDto(
        type = AuditRecordType.CREATE,
        commitDate = Instant.parse("2026-01-10T10:00:00Z"),
        changes = listOf(
          FieldChange(field = "referenceNumber", value = initialReference),
          FieldChange(field = "localAuthorityAreaId", value = initialLa.id.toString()),
          FieldChange(field = "status", value = "SUBMITTED"),
        ),
      )
      val firstRefUpdate = buildAuditRecordDto(
        type = AuditRecordType.UPDATE,
        commitDate = Instant.parse("2026-01-11T13:00:00Z"),
        changes = listOf(
          FieldChange(
            field = "referenceNumber",
            value = firstRef,
            oldValue = initialReference,
          ),
        ),
      )
      val firstLaUpdate = laChange(
        from = initialLa.id,
        to = firstLa.id,
        at = "2026-01-11T13:02:00Z",
      )
      val secondRefUpdate = buildAuditRecordDto(
        type = AuditRecordType.UPDATE,
        commitDate = Instant.parse("2026-01-12T15:00:00Z"),
        changes = listOf(
          FieldChange(
            field = "referenceNumber",
            value = secondRef,
            oldValue = firstRef,
          ),
        ),
      )
      val secondLaUpdate = laChange(
        from = firstLa.id,
        to = secondLa.id,
        at = "2026-01-12T15:12:00Z",
      )
      val thirdLaUpdate = laChange(
        from = secondLa.id,
        to = thirdLa.id,
        at = "2026-01-13T18:00:00Z",
      )
      val thirdRefUpdate = buildAuditRecordDto(
        type = AuditRecordType.UPDATE,
        commitDate = Instant.parse("2026-01-13T18:00:01Z"),
        changes = listOf(
          FieldChange(
            field = "referenceNumber",
            value = thirdRef,
            oldValue = secondRef,
          ),
        ),
      )
      val fourthRefUpdate = buildAuditRecordDto(
        type = AuditRecordType.UPDATE,
        commitDate = Instant.parse("2026-01-13T23:00:00Z"),
        changes = listOf(
          FieldChange(
            field = "referenceNumber",
            value = fourthRef,
            oldValue = thirdRef,
          ),
        ),
      )
      val fourthLaUpdate = laChange(
        from = thirdLa.id,
        to = fourthLa.id,
        at = "2026-01-14T11:00:03Z",
      )

      every {
        otherAccommodationReferralRepository.findByIdAndCrnWithNotes(otherAccommodationReferralEntity.id, crn)
      } returns otherAccommodationReferralEntity
      every {
        auditService.fullAuditHistory(otherAccommodationReferralEntity.id, OtherAccommodationReferralEntity::class.java)
      } returns listOf(
        createRecord, // 1. LA = Cherwell
        firstRefUpdate, // 2. Reference changed only → Cherwell
        firstLaUpdate, // 3. LA changes: Cherwell → Oxford
        secondRefUpdate, // 4. reference changed only → Oxford
        secondLaUpdate, // 5. LA changes: Oxford → Gloucester
        thirdLaUpdate, // 6. LA changes: Gloucester → Cambridge
        thirdRefUpdate, // 7. reference changed only → Cambridge
        fourthRefUpdate, // 8. reference changed only → Cambridge
        fourthLaUpdate, // 9. LA changes: Cambridge → Stroud (final state)
      )
      every {
        localAuthorityAreaRepository.findAllById(
          setOf(initialLa.id, firstLa.id, secondLa.id, thirdLa.id, fourthLa.id),
        )
      } returns listOf(initialLa, firstLa, secondLa, thirdLa, fourthLa)

      val result = service.getOtherAccommodationReferralTimeline(otherAccommodationReferralEntity.id, crn)

      assertThat(result.data).hasSize(9)

      // 9. Final state: Cambridge → Stroud
      assertThat(result.data[0].commitDate).isEqualTo(fourthLaUpdate.commitDate)
      assertThat(result.data[0].extraInformation?.get("localAuthorityAreaName")).isEqualTo("Stroud")
      // 8. reference changed only → Cambridge
      assertThat(result.data[1].commitDate).isEqualTo(fourthRefUpdate.commitDate)
      assertThat(result.data[1].extraInformation?.get("localAuthorityAreaName")).isEqualTo("Cambridge")
      // 7. reference changed only → Cambridge
      assertThat(result.data[2].commitDate).isEqualTo(thirdRefUpdate.commitDate)
      assertThat(result.data[2].extraInformation?.get("localAuthorityAreaName")).isEqualTo("Cambridge")
      // 6. LA changes: Gloucester → Cambridge
      assertThat(result.data[3].commitDate).isEqualTo(thirdLaUpdate.commitDate)
      assertThat(result.data[3].extraInformation?.get("localAuthorityAreaName")).isEqualTo("Cambridge")
      // 5. LA changes: Oxford → Gloucester
      assertThat(result.data[4].commitDate).isEqualTo(secondLaUpdate.commitDate)
      assertThat(result.data[4].extraInformation?.get("localAuthorityAreaName")).isEqualTo("Gloucester")
      // 4. reference changed only → Oxford
      assertThat(result.data[5].commitDate).isEqualTo(secondRefUpdate.commitDate)
      assertThat(result.data[5].extraInformation?.get("localAuthorityAreaName")).isEqualTo("Oxford")
      // 3. LA changes: Cherwell → Oxford
      assertThat(result.data[6].commitDate).isEqualTo(firstLaUpdate.commitDate)
      assertThat(result.data[6].extraInformation?.get("localAuthorityAreaName")).isEqualTo("Oxford")
      // 2. Reference changed only → Cherwell
      assertThat(result.data[7].commitDate).isEqualTo(firstRefUpdate.commitDate)
      assertThat(result.data[7].extraInformation?.get("localAuthorityAreaName")).isEqualTo("Cherwell")
      // 1. LA = Cherwell
      assertThat(result.data[8].commitDate).isEqualTo(createRecord.commitDate)
      assertThat(result.data[8].extraInformation?.get("localAuthorityAreaName")).isEqualTo("Cherwell")
    }

    @Test
    fun `should merge multiple notes from different authors with audit history sorted descending`() {
      val localAuthorityAreaId = UUID.randomUUID()
      val user1Id = UUID.randomUUID()
      val user2Id = UUID.randomUUID()
      val otherAccommodationReferralEntity = buildOtherAccommodationReferralEntity(caseId = caseId, crn = crn, localAuthorityAreaId = localAuthorityAreaId)
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
      val laEntity = buildLocalAuthorityAreaEntity(id = localAuthorityAreaId, name = "Cherwell")
      val createRecord = buildAuditRecordDto(
        type = AuditRecordType.CREATE,
        commitDate = Instant.parse("2026-01-10T10:00:00Z"),
        changes = listOf(
          FieldChange(field = "localAuthorityAreaId", value = localAuthorityAreaId.toString()),
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
      every { localAuthorityAreaRepository.findAllById(setOf(localAuthorityAreaId)) } returns listOf(laEntity)
      every { userRepository.findAllById(setOf(user1Id, user2Id)) } returns listOf(noteAuthor1, noteAuthor2)

      val result = service.getOtherAccommodationReferralTimeline(otherAccommodationReferralEntity.id, crn)

      assertThat(result.data).hasSize(4)
      assertThat(result.data[0].type).isEqualTo(AuditRecordType.NOTE)
      assertThat(result.data[0].commitDate).isEqualTo(note2CreatedAt)
      assertThat(result.data[0].author).isEqualTo("Second user")
      assertThat(result.data[0].authorDetails).isEqualTo(
        AssignedToDto(forename = "Second", surname = "user", username = "user2"),
      )
      assertThat(result.data[0].extraInformation?.get("localAuthorityAreaName")).isNull()
      assertThat(result.data[1].type).isEqualTo(AuditRecordType.UPDATE)
      assertThat(result.data[1].commitDate).isEqualTo(updateRecord.commitDate)
      assertThat(result.data[1].extraInformation?.get("localAuthorityAreaName")).isEqualTo("Cherwell")
      assertThat(result.data[2].type).isEqualTo(AuditRecordType.NOTE)
      assertThat(result.data[2].commitDate).isEqualTo(note1CreatedAt)
      assertThat(result.data[2].author).isEqualTo("First user")
      assertThat(result.data[2].authorDetails).isEqualTo(
        AssignedToDto(forename = "First", surname = "user", username = "user1"),
      )
      assertThat(result.data[2].extraInformation?.get("localAuthorityAreaName")).isNull()
      assertThat(result.data[3].type).isEqualTo(AuditRecordType.CREATE)
      assertThat(result.data[3].commitDate).isEqualTo(createRecord.commitDate)
      assertThat(result.data[3].extraInformation?.get("localAuthorityAreaName")).isEqualTo("Cherwell")
    }

    @Test
    fun `should throw NotFoundException when not found`() {
      val id = UUID.randomUUID()
      every { otherAccommodationReferralRepository.findByIdAndCrnWithNotes(id, crn) } returns null

      assertThatThrownBy { service.getOtherAccommodationReferralTimeline(id, crn) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("OtherAccommodationReferralEntity not found for [id=$id, crn=$crn]")
    }

    private fun laChange(from: UUID, to: UUID, at: String) = buildAuditRecordDto(
      type = AuditRecordType.UPDATE,
      commitDate = Instant.parse(at),
      changes = listOf(
        FieldChange(
          field = "localAuthorityAreaId",
          value = to.toString(),
          oldValue = from.toString(),
        ),
      ),
    )
  }
}
