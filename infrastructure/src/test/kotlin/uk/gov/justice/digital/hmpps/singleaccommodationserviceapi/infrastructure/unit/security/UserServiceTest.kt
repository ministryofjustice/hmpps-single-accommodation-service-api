package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.security

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.ProbationIntegrationDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildStaffDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.HttpAuthService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService

@ExtendWith(value = [MockKExtension::class])
class UserServiceTest {
  @MockK
  lateinit var httpAuthService: HttpAuthService
  @MockK
  lateinit var userRepository: UserRepository
  @MockK
  lateinit var probationIntegrationDeliusCachingService: ProbationIntegrationDeliusCachingService

  @InjectMockKs
  lateinit var userService: UserService

  @Nested
  inner class GetExistingUserOrCreate {
    private val username = "SOMEPERSON"

    @Test
    fun `getExistingUserOrCreate should throw NotFoundException when staff-detail not found`() {
      every { userRepository.findByUsernameAndAuthSource(username, AuthSource.DELIUS) } returns null
      every { probationIntegrationDeliusCachingService.getStaffDetail(username) } returns null

      assertThatThrownBy { userService.getExistingUserOrCreate(username) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessageContaining(username)
    }

    @Test
    fun `getExistingUserOrCreate returns existing user`() {
      val user = buildUserEntity()
      every { userRepository.findByUsernameAndAuthSource(username, AuthSource.DELIUS) } returns user

      val result = userService.getExistingUserOrCreate(username)

      assertThat(result).isEqualTo(user)
      verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `getExistingUserOrCreate creates new user`() {
      val deliusUser = buildStaffDetail()
      every { userRepository.findByUsernameAndAuthSource(username, AuthSource.DELIUS) } returns null
      every { userRepository.save(any()) } answers { it.invocation.args[0] as UserEntity }
      every { probationIntegrationDeliusCachingService.getStaffDetail(username) } returns deliusUser

      val result = userService.getExistingUserOrCreate(username)

      assertThat(result.name).isEqualTo(deliusUser.name.deliusName())
      assertThat(result.email).isEqualTo(deliusUser.email)
      assertThat(result.telephoneNumber).isEqualTo(deliusUser.telephoneNumber)
      assertThat(result.deliusStaffCode).isEqualTo(deliusUser.code)
      assertThat(result.isEnabled).isTrue()
      assertThat(result.isActive).isTrue()
      assertThat(result.nomisStaffId).isNull()
      assertThat(result.nomisAccountType).isNull()
      assertThat(result.nomisActiveCaseloadId).isNull()

      verify(exactly = 1) { userRepository.save(any()) }
    }
  }
}