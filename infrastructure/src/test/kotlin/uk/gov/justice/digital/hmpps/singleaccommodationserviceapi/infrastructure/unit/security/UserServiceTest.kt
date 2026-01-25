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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.nomisuserroles.NomisUserRolesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.ProbationIntegrationDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildNomisUserDetail
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

  @MockK
  lateinit var nomisUserRolesService: NomisUserRolesService

  @InjectMockKs
  lateinit var userService: UserService

  @Nested
  inner class GetExistingDeliusUserOrCreate {
    private val username = "SOMEPERSON"

    @Test
    fun `getExistingDeliusUserOrCreate should throw NotFoundException when staff-detail not found`() {
      every { userRepository.findByUsernameAndAuthSource(username, AuthSource.DELIUS) } returns null
      every { probationIntegrationDeliusCachingService.getStaffDetail(username) } returns null

      assertThatThrownBy { userService.getExistingDeliusUserOrCreate(username) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessageContaining(username)
    }

    @Test
    fun `getExistingDeliusUserOrCreate returns existing user`() {
      val user = buildUserEntity()
      every { userRepository.findByUsernameAndAuthSource(username, AuthSource.DELIUS) } returns user

      val result = userService.getExistingDeliusUserOrCreate(username)

      assertThat(result).isEqualTo(user)
      verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `getExistingDeliusUserOrCreate creates new user`() {
      val deliusUser = buildStaffDetail()
      every { userRepository.findByUsernameAndAuthSource(username, AuthSource.DELIUS) } returns null
      every { userRepository.save(any()) } answers { it.invocation.args[0] as UserEntity }
      every { probationIntegrationDeliusCachingService.getStaffDetail(username) } returns deliusUser

      val result = userService.getExistingDeliusUserOrCreate(username)

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

  @Nested
  inner class GetAndUpdateNomisUserOrCreate {
    private val username = "SOMENOMISPERSON"

    @Test
    fun `getAndUpdateNomisUserOrCreate should throw NotFoundException when user-details not found`() {
      every { nomisUserRolesService.getUserDetailsForMe() } returns null

      assertThatThrownBy { userService.getAndUpdateNomisUserOrCreate(username) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessageContaining(username)
    }

    @Test
    fun `getAndUpdateNomisUserOrCreate returns and updates existing nomis user`() {
      val nomisUser = buildNomisUserDetail(
        username = username,
        primaryEmail = "changed.email@gmail.com",
        activeCaseloadId = "1234",
      )
      val nomisUserEntity = buildUserEntity(
        username = username,
        authSource = AuthSource.NOMIS,
      )
      every { nomisUserRolesService.getUserDetailsForMe() } returns nomisUser
      every { userRepository.findByUsernameAndAuthSource(username, AuthSource.NOMIS) } returns nomisUserEntity
      every { userRepository.save(any()) } answers { it.invocation.args[0] as UserEntity }

      val result = userService.getAndUpdateNomisUserOrCreate(username)

      assertThat(result.username).isEqualTo(nomisUser.username)
      assertThat(result.email).isEqualTo(nomisUser.primaryEmail)
      assertThat(result.nomisActiveCaseloadId).isEqualTo(nomisUser.activeCaseloadId)

      verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `getAndUpdateNomisUserOrCreate creates new nomis user`() {
      val nomisUser = buildNomisUserDetail(
        username = username,
      )
      every { nomisUserRolesService.getUserDetailsForMe() } returns nomisUser
      every { userRepository.findByUsernameAndAuthSource(username, AuthSource.NOMIS) } returns null
      every { userRepository.save(any()) } answers { it.invocation.args[0] as UserEntity }

      val result = userService.getAndUpdateNomisUserOrCreate(username)

      assertThat(result.username).isEqualTo(nomisUser.username)
      assertThat(result.authSource).isEqualTo(AuthSource.NOMIS)
      assertThat(result.name).isEqualTo("${nomisUser.firstName} ${nomisUser.lastName}")
      assertThat(result.email).isEqualTo(nomisUser.primaryEmail)
      assertThat(result.nomisStaffId).isEqualTo(nomisUser.staffId)
      assertThat(result.nomisAccountType).isEqualTo(nomisUser.accountType)
      assertThat(result.nomisActiveCaseloadId).isEqualTo(nomisUser.activeCaseloadId)
      assertThat(result.isEnabled).isEqualTo(nomisUser.active)
      assertThat(result.isActive).isEqualTo(nomisUser.enabled)
      assertThat(result.telephoneNumber).isNull()
      assertThat(result.deliusStaffCode).isNull()

      verify(exactly = 1) { userRepository.save(any()) }
    }
  }
}
