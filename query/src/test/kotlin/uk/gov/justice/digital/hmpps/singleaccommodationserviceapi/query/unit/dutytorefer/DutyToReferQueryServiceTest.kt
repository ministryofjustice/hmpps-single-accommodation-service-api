package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.dutytorefer

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildLocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import java.util.UUID

@ExtendWith(MockKExtension::class)
class DutyToReferQueryServiceTest {

  @MockK
  lateinit var dutyToReferRepository: DutyToReferRepository

  @MockK
  lateinit var userRepository: UserRepository

  @MockK
  lateinit var localAuthorityAreaRepository: LocalAuthorityAreaRepository

  @InjectMockKs
  lateinit var service: DutyToReferQueryService

  private val crn = "X12345"

  @Nested
  inner class GetDutyToRefer {

    @Test
    fun `should return NOT_STARTED with null submission when no DTR exists`() {
      every { dutyToReferRepository.findFirstByCrnOrderByCreatedAtDesc(crn) } returns null

      val result = service.getDutyToRefer(crn)

      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.status).isEqualTo(DtrStatus.NOT_STARTED)
      assertThat(result.submission).isNull()
    }

    @Test
    fun `should return DTR with submission and localAuthorityAreaName when DTR exists`() {
      val createdByUserId = UUID.randomUUID()
      val localAuthorityAreaId = UUID.randomUUID()
      val dtrEntity = buildDutyToReferEntity(
        crn = crn,
        localAuthorityAreaId = localAuthorityAreaId,
        createdByUserId = createdByUserId,
      )
      val userEntity = buildUserEntity()
      val localAuthorityAreaEntity = buildLocalAuthorityAreaEntity(
        id = localAuthorityAreaId,
        name = "Test Local Authority",
      )

      every { dutyToReferRepository.findFirstByCrnOrderByCreatedAtDesc(crn) } returns dtrEntity
      every { userRepository.findByIdOrNull(createdByUserId) } returns userEntity
      every { localAuthorityAreaRepository.findByIdOrNull(localAuthorityAreaId) } returns localAuthorityAreaEntity

      val result = service.getDutyToRefer(crn)

      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.status).isEqualTo(DtrStatus.SUBMITTED)
      assertThat(result.submission).isNotNull()
      val submission = result.submission!!
      assertThat(submission.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
      assertThat(submission.localAuthorityAreaName).isEqualTo(localAuthorityAreaEntity.name)
      assertThat(submission.createdBy).isEqualTo(userEntity.name)
    }
  }
}
