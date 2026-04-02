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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildLocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
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
}
