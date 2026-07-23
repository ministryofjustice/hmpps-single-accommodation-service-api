package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.dutytorefer

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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FieldChange
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.Instant
import java.time.LocalDate
import java.util.Optional
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DtrStatus as EntityDtrStatus

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
  private val clock = MutableClock()

  @Nested
  inner class GetDutyToReferByCrnAndCaseEntity {

    @Test
    fun `should return null when no DTR exists`() {
      val caseEntity = buildCaseEntity(id = caseId) { withCrn(crn) }
      every { dutyToReferRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns null

      val result = service.getDutyToRefer(caseEntity, crn)

      assertThat(result).isNull()
    }

    @Test
    fun `should return DTR with submission and localAuthorityAreaName when DTR exists`() {
      val caseEntity = buildCaseEntity(id = caseId) { withCrn(crn) }
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
      every { dutyToReferRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns dtrEntity
      every { userRepository.findByIdOrNull(createdByUserId) } returns userEntity
      every { localAuthorityAreaRepository.findByIdOrNull(localAuthorityAreaId) } returns localAuthorityAreaEntity

      val result = service.getDutyToRefer(caseEntity, crn)!!

      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.caseId).isEqualTo(caseId)
      assertThat(result.status).isEqualTo(DtrStatus.SUBMITTED)
      assertThat(result.submission).isNotNull()
      val submission = result.submission!!
      assertThat(submission.localAuthority.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
      assertThat(submission.localAuthority.localAuthorityAreaName).isEqualTo(localAuthorityAreaEntity.name)
      assertThat(submission.createdBy).isEqualTo(userEntity.displayName())
    }
  }

  @Nested
  inner class GetDutyToReferHistory {

    @Test
    fun `should return empty list when no matching DTRs exist`() {
      val caseEntity = buildCaseEntity(id = caseId) { withCrn(crn) }
      every { dutyToReferRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns null
      every {
        dutyToReferRepository.findByCaseIdAndStatusInOrderByCreatedAtDesc(caseId, any())
      } returns emptyList()

      assertThat(service.getDutyToReferHistory(caseEntity, crn)).isEmpty()
    }

    @Test
    fun `should query for accepted, not accepted and withdrawn statuses only and map the results`() {
      val caseEntity = buildCaseEntity(id = caseId) { withCrn(crn) }
      val createdByUserId = UUID.randomUUID()
      val localAuthorityAreaId = UUID.randomUUID()

      val acceptedDtr = buildDutyToReferEntity(
        caseId = caseId,
        localAuthorityAreaId = localAuthorityAreaId,
        createdByUserId = createdByUserId,
        status = EntityDtrStatus.ACCEPTED,
      )
      val userEntity = buildUserEntity(id = createdByUserId)
      val localAuthorityAreaEntity = buildLocalAuthorityAreaEntity(
        id = localAuthorityAreaId,
        name = "Test Local Authority",
      )

      val statusSlot = slot<List<EntityDtrStatus>>()
      every { dutyToReferRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns null
      every {
        dutyToReferRepository.findByCaseIdAndStatusInOrderByCreatedAtDesc(caseId, capture(statusSlot))
      } returns listOf(acceptedDtr)
      every { userRepository.findAllById(setOf(createdByUserId)) } returns listOf(userEntity)
      every { localAuthorityAreaRepository.findAllById(setOf(localAuthorityAreaId)) } returns listOf(localAuthorityAreaEntity)

      val result = service.getDutyToReferHistory(caseEntity, crn)

      assertThat(statusSlot.captured).containsExactlyInAnyOrder(
        EntityDtrStatus.ACCEPTED,
        EntityDtrStatus.NOT_ACCEPTED,
        EntityDtrStatus.WITHDRAWN,
      )
      assertThat(result).hasSize(1)
      assertThat(result[0].status).isEqualTo(DtrStatus.ACCEPTED)
      assertThat(result[0].crn).isEqualTo(crn)
      assertThat(result[0].submission!!.localAuthority.localAuthorityAreaName).isEqualTo("Test Local Authority")
    }

    @Test
    fun `should exclude the active referral from history`() {
      val caseEntity = buildCaseEntity(id = caseId) { withCrn(crn) }
      val historicUserId = UUID.randomUUID()
      val historicLaId = UUID.randomUUID()

      val activeDtr = buildDutyToReferEntity(
        id = UUID.randomUUID(),
        caseId = caseId,
        submissionDate = LocalDate.now().minusMonths(5),
        status = EntityDtrStatus.ACCEPTED,
      )
      val historicDtr = buildDutyToReferEntity(
        id = UUID.randomUUID(),
        caseId = caseId,
        localAuthorityAreaId = historicLaId,
        createdByUserId = historicUserId,
        submissionDate = LocalDate.now().minusMonths(13),
        status = EntityDtrStatus.WITHDRAWN,
      )

      every { dutyToReferRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns activeDtr
      every {
        dutyToReferRepository.findByCaseIdAndStatusInOrderByCreatedAtDesc(caseId, any())
      } returns listOf(activeDtr, historicDtr)
      every { userRepository.findAllById(setOf(historicUserId)) } returns listOf(buildUserEntity(id = historicUserId))
      every { localAuthorityAreaRepository.findAllById(setOf(historicLaId)) } returns
        listOf(buildLocalAuthorityAreaEntity(id = historicLaId, name = "Historic LA"))

      val result = service.getDutyToReferHistory(caseEntity, crn)

      assertThat(result).hasSize(1)
      assertThat(result[0].submission!!.id).isEqualTo(historicDtr.id)
      assertThat(result[0].status).isEqualTo(DtrStatus.WITHDRAWN)
    }

    @Test
    fun `should include the latest referral in history when it is withdrawn`() {
      val caseEntity = buildCaseEntity(id = caseId) { withCrn(crn) }
      val userId = UUID.randomUUID()
      val laId = UUID.randomUUID()
      val withdrawnLatest = buildDutyToReferEntity(
        id = UUID.randomUUID(),
        caseId = caseId,
        localAuthorityAreaId = laId,
        createdByUserId = userId,
        submissionDate = LocalDate.now().minusMonths(1),
        status = EntityDtrStatus.WITHDRAWN,
      )

      every { dutyToReferRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns withdrawnLatest
      every {
        dutyToReferRepository.findByCaseIdAndStatusInOrderByCreatedAtDesc(caseId, any())
      } returns listOf(withdrawnLatest)
      every { userRepository.findAllById(setOf(userId)) } returns listOf(buildUserEntity(id = userId))
      every { localAuthorityAreaRepository.findAllById(setOf(laId)) } returns
        listOf(buildLocalAuthorityAreaEntity(id = laId, name = "LA"))

      val result = service.getDutyToReferHistory(caseEntity, crn)

      assertThat(result).hasSize(1)
      assertThat(result[0].submission!!.id).isEqualTo(withdrawnLatest.id)
    }

    @Test
    fun `should include the latest referral in history when it is expired`() {
      val caseEntity = buildCaseEntity(id = caseId) { withCrn(crn) }
      val userId = UUID.randomUUID()
      val laId = UUID.randomUUID()
      val expiredLatest = buildDutyToReferEntity(
        id = UUID.randomUUID(),
        caseId = caseId,
        localAuthorityAreaId = laId,
        createdByUserId = userId,
        submissionDate = LocalDate.now().minusMonths(7),
        status = EntityDtrStatus.ACCEPTED,
      )

      every { dutyToReferRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns expiredLatest
      every {
        dutyToReferRepository.findByCaseIdAndStatusInOrderByCreatedAtDesc(caseId, any())
      } returns listOf(expiredLatest)
      every { userRepository.findAllById(setOf(userId)) } returns listOf(buildUserEntity(id = userId))
      every { localAuthorityAreaRepository.findAllById(setOf(laId)) } returns
        listOf(buildLocalAuthorityAreaEntity(id = laId, name = "LA"))

      val result = service.getDutyToReferHistory(caseEntity, crn)

      assertThat(result).hasSize(1)
      assertThat(result[0].submission!!.id).isEqualTo(expiredLatest.id)
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
        submissionDate = LocalDate.now().minusMonths(5),
      )
      val userEntity = buildUserEntity()
      val localAuthorityAreaEntity = buildLocalAuthorityAreaEntity(
        id = localAuthorityAreaId,
        name = "Test Local Authority",
      )

      every { dutyToReferRepository.findByIdAndCrn(id, crn) } returns dtrEntity
      every { dutyToReferRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns dtrEntity
      every { userRepository.findByIdOrNull(createdByUserId) } returns userEntity
      every { localAuthorityAreaRepository.findByIdOrNull(localAuthorityAreaId) } returns localAuthorityAreaEntity

      val result = service.getDutyToRefer(crn, id)

      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.caseId).isEqualTo(caseId)
      assertThat(result.status).isEqualTo(DtrStatus.SUBMITTED)
      assertThat(result.active).isTrue()
      assertThat(result.submission).isNotNull()
      val submission = result.submission!!
      assertThat(submission.id).isEqualTo(id)
      assertThat(submission.localAuthority.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
      assertThat(submission.localAuthority.localAuthorityAreaName).isEqualTo(localAuthorityAreaEntity.name)
      assertThat(submission.createdBy).isEqualTo(userEntity.displayName())
    }

    @Test
    fun `should return active false when the referral is expired`() {
      val dtrEntity = buildDutyToReferEntity(
        id = id,
        caseId = caseId,
        createdByUserId = UUID.randomUUID(),
        submissionDate = LocalDate.now().minusMonths(7),
        status = EntityDtrStatus.SUBMITTED,
      )

      every { dutyToReferRepository.findByIdAndCrn(id, crn) } returns dtrEntity
      every { dutyToReferRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns dtrEntity
      every { userRepository.findByIdOrNull(any()) } returns buildUserEntity()
      every { localAuthorityAreaRepository.findByIdOrNull(any()) } returns buildLocalAuthorityAreaEntity()

      assertThat(service.getDutyToRefer(crn, id).active).isFalse()
    }

    @Test
    fun `should return active false when the referral is withdrawn`() {
      val dtrEntity = buildDutyToReferEntity(
        id = id,
        caseId = caseId,
        createdByUserId = UUID.randomUUID(),
        submissionDate = LocalDate.now().minusMonths(5),
        status = EntityDtrStatus.WITHDRAWN,
      )

      every { dutyToReferRepository.findByIdAndCrn(id, crn) } returns dtrEntity
      every { dutyToReferRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns dtrEntity
      every { userRepository.findByIdOrNull(any()) } returns buildUserEntity()
      every { localAuthorityAreaRepository.findByIdOrNull(any()) } returns buildLocalAuthorityAreaEntity()

      assertThat(service.getDutyToRefer(crn, id).active).isFalse()
    }

    @Test
    fun `should return active false when a newer referral supersedes this one`() {
      val dtrEntity = buildDutyToReferEntity(
        id = id,
        caseId = caseId,
        createdByUserId = UUID.randomUUID(),
        submissionDate = LocalDate.now().minusMonths(5),
        status = EntityDtrStatus.SUBMITTED,
      )
      val newerDtr = buildDutyToReferEntity(
        id = UUID.randomUUID(),
        caseId = caseId,
        submissionDate = LocalDate.now().minusMonths(4),
        status = EntityDtrStatus.SUBMITTED,
      )

      every { dutyToReferRepository.findByIdAndCrn(id, crn) } returns dtrEntity
      every { dutyToReferRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId) } returns newerDtr
      every { userRepository.findByIdOrNull(any()) } returns buildUserEntity()
      every { localAuthorityAreaRepository.findByIdOrNull(any()) } returns buildLocalAuthorityAreaEntity()

      assertThat(service.getDutyToRefer(crn, id).active).isFalse()
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
      every { caseRepository.findWithIdentifiersById(caseId) } returns buildCaseEntity { withCrn(crn) }
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
      assertThat(submission.createdBy).isEqualTo(userEntity.displayName())
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
          FieldChange(field = "localAuthorityAreaId", value = localAuthorityAreaId.toString()),
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

      every { dutyToReferRepository.findByIdAndCrnWithNotes(dtrEntity.id, crn) } returns dtrEntity
      every {
        auditService.fullAuditHistory(dtrEntity.id, DutyToReferEntity::class.java)
      } returns listOf(createRecord, updateRecord)
      every { localAuthorityAreaRepository.findAllById(setOf(localAuthorityAreaId)) } returns listOf(laEntity)

      val result = service.getDutyToReferTimeline(dtrEntity.id, crn)

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
      val dtrEntity = buildDutyToReferEntity(caseId = caseId, localAuthorityAreaId = updatedLaId)
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

      val dtrEntity = buildDutyToReferEntity(
        caseId = caseId,
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

      every { dutyToReferRepository.findByIdAndCrnWithNotes(dtrEntity.id, crn) } returns dtrEntity
      every {
        auditService.fullAuditHistory(dtrEntity.id, DutyToReferEntity::class.java)
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

      val result = service.getDutyToReferTimeline(dtrEntity.id, crn)

      assertThat(result.data).hasSize(9)

      // 9. Final state: Cambridge → Stroud
      assertThat(result.data[0].extraInformation?.get("localAuthorityAreaName")).isEqualTo(fourthLa.name)
      assertThat(result.data[0].commitDate).isEqualTo(fourthLaUpdate.commitDate)

      // 8. Reference update (LA still Cambridge)
      assertThat(result.data[1].extraInformation?.get("localAuthorityAreaName")).isEqualTo(thirdLa.name)
      assertThat(result.data[1].commitDate).isEqualTo(fourthRefUpdate.commitDate)

      // 7. Reference update (LA still Cambridge)
      assertThat(result.data[2].extraInformation?.get("localAuthorityAreaName")).isEqualTo(thirdLa.name)
      assertThat(result.data[2].commitDate).isEqualTo(thirdRefUpdate.commitDate)

      // 6. LA change: Gloucester → Cambridge
      assertThat(result.data[3].extraInformation?.get("localAuthorityAreaName")).isEqualTo(thirdLa.name)
      assertThat(result.data[3].commitDate).isEqualTo(thirdLaUpdate.commitDate)

      // 5. LA change: Oxford → Gloucester
      assertThat(result.data[4].extraInformation?.get("localAuthorityAreaName")).isEqualTo(secondLa.name)
      assertThat(result.data[4].commitDate).isEqualTo(secondLaUpdate.commitDate)

      // 4. Reference update (LA still Oxford)
      assertThat(result.data[5].extraInformation?.get("localAuthorityAreaName")).isEqualTo(firstLa.name)
      assertThat(result.data[5].commitDate).isEqualTo(secondRefUpdate.commitDate)

      // 3. LA change: Cherwell → Oxford
      assertThat(result.data[6].extraInformation?.get("localAuthorityAreaName")).isEqualTo(firstLa.name)
      assertThat(result.data[6].commitDate).isEqualTo(firstLaUpdate.commitDate)

      // 2. Reference update (LA still Cherwell)
      assertThat(result.data[7].extraInformation?.get("localAuthorityAreaName")).isEqualTo(initialLa.name)
      assertThat(result.data[7].commitDate).isEqualTo(firstRefUpdate.commitDate)

      // 1. Create event LA set to Cherwell
      assertThat(result.data[8].extraInformation?.get("localAuthorityAreaName")).isEqualTo(initialLa.name)
      assertThat(result.data[8].commitDate).isEqualTo(createRecord.commitDate)
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
          FieldChange(field = "localAuthorityAreaId", value = localAuthorityAreaId.toString()),
        ),
      )
      val updateRecord = buildAuditRecordDto(
        type = AuditRecordType.UPDATE,
        commitDate = Instant.parse("2026-01-12T10:00:00Z"),
        changes = listOf(
          FieldChange(field = "status", value = "ACCEPTED", oldValue = "SUBMITTED"),
        ),
      )
      val noteAuthor1 = buildUserEntity(id = user1Id, forename = "First", surname = "user")
      val noteAuthor2 = buildUserEntity(id = user2Id, forename = "Second", surname = "user")

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
      assertThat(result.data[0].extraInformation?.get("localAuthorityAreaName")).isNull()
      assertThat(result.data[1].type).isEqualTo(AuditRecordType.UPDATE)
      assertThat(result.data[1].commitDate).isEqualTo(updateRecord.commitDate)
      assertThat(result.data[1].extraInformation?.get("localAuthorityAreaName")).isEqualTo("Cherwell")
      assertThat(result.data[2].type).isEqualTo(AuditRecordType.NOTE)
      assertThat(result.data[2].commitDate).isEqualTo(note1CreatedAt)
      assertThat(result.data[2].author).isEqualTo("First user")
      assertThat(result.data[2].extraInformation?.get("localAuthorityAreaName")).isNull()
      assertThat(result.data[3].type).isEqualTo(AuditRecordType.CREATE)
      assertThat(result.data[3].commitDate).isEqualTo(createRecord.commitDate)
      assertThat(result.data[3].extraInformation?.get("localAuthorityAreaName")).isEqualTo("Cherwell")
    }

    @Test
    fun `should throw NotFoundException when not found`() {
      val id = UUID.randomUUID()
      every { dutyToReferRepository.findByIdAndCrnWithNotes(id, crn) } returns null

      assertThatThrownBy { service.getDutyToReferTimeline(id, crn) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("DutyToReferEntity not found for [id=$id, crn=$crn]")
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
