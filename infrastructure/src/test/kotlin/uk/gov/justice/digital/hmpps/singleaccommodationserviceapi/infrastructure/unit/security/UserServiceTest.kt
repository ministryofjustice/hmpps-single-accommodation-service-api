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
import org.springframework.security.oauth2.jwt.Jwt
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.nomisuserroles.NomisUserRolesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildNomisUserDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildStaffDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.HttpAuthService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.Username

@ExtendWith(value = [MockKExtension::class])
class UserServiceTest {
  @MockK
  lateinit var httpAuthService: HttpAuthService

  @MockK
  lateinit var userRepository: UserRepository

  @MockK
  lateinit var approvedPremisesAndDeliusCachingService: ApprovedPremisesAndDeliusCachingService

  @MockK
  lateinit var nomisUserRolesService: NomisUserRolesService

  @InjectMockKs
  lateinit var userService: UserService

  @Nested
  inner class GetAndUpsertDeliusUser {
    private val username = Username("SOMEPERSON")

    @Test
    fun `getAndUpsertDeliusUser should throw NotFoundException when staff-detail not found`() {
      every { userRepository.findByUsernameAndAuthSource(username, AuthSource.DELIUS) } returns null
      every { approvedPremisesAndDeliusCachingService.getStaffDetail(username.value) } returns null

      assertThatThrownBy { userService.getAndUpsertDeliusUser(username) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("StaffDetail not found for [username=$username]")
    }

    @Test
    fun `getAndUpsertDeliusUser updates and returns existing user`() {
      val existingUser = buildUserEntity()
      every { userRepository.findByUsernameAndAuthSource(username, AuthSource.DELIUS) } returns existingUser

      every { userRepository.save(existingUser) } returnsArgument 0

      val result = userService.getAndUpsertDeliusUser(username)

      assertThat(result).isEqualTo(existingUser)

      verify(exactly = 0) { approvedPremisesAndDeliusCachingService.getStaffDetail(any()) }
      verify(exactly = 0) { userRepository.save(existingUser) }
    }

    @Test
    fun `getAndUpsertDeliusUser creates new user`() {
      val deliusUser = buildStaffDetail()
      every { userRepository.findByUsernameAndAuthSource(username, AuthSource.DELIUS) } returns null
      every { userRepository.save(any()) } answers { it.invocation.args[0] as UserEntity }
      every { approvedPremisesAndDeliusCachingService.getStaffDetail(username.value) } returns deliusUser

      val result = userService.getAndUpsertDeliusUser(username)

      assertThat(result.forename).isEqualTo(deliusUser.name.forename)
      assertThat(result.middleNames).isEqualTo(deliusUser.name.middleName)
      assertThat(result.surname).isEqualTo(deliusUser.name.surname)
      assertThat(result.email).isEqualTo(deliusUser.email)
      assertThat(result.telephoneNumber).isEqualTo(deliusUser.telephoneNumber)
      assertThat(result.deliusStaffCode).isEqualTo(deliusUser.code)
      assertThat(result.isEnabled).isNull()
      assertThat(result.isActive).isTrue()
      assertThat(result.nomisStaffId).isNull()
      assertThat(result.nomisAccountType).isNull()
      assertThat(result.nomisActiveCaseloadId).isNull()

      verify(exactly = 1) { userRepository.save(any()) }
    }
  }

  @Nested
  inner class GetAndUpdateNomisUserOrCreate {
    private val username = Username("somenomisperson")
    private val jwt = Jwt.withTokenValue("token")
      .header("Authroization", "jwt")
      .claim("username", username)
      .build()

    @Test
    fun `getAndUpdateNomisUserOrCreate should throw NotFoundException when user-details not found`() {
      every { nomisUserRolesService.getUserDetailsForMe(jwt) } returns null

      assertThatThrownBy { userService.getAndUpdateNomisUserOrCreate(username, jwt) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("NomisUserDetail not found for [username=$username]")
    }

    @Test
    fun `getAndUpdateNomisUserOrCreate returns and updates existing nomis user`() {
      val nomisUser = buildNomisUserDetail(
        username = username.value,
        primaryEmail = "changed.email@gmail.com",
        activeCaseloadId = "1234",
      )
      val nomisUserEntity = buildUserEntity(
        username = username.value,
        authSource = AuthSource.NOMIS,
      )
      every { nomisUserRolesService.getUserDetailsForMe(jwt) } returns nomisUser
      every { userRepository.findByUsernameAndAuthSource(username, AuthSource.NOMIS) } returns nomisUserEntity
      every { userRepository.save(any()) } answers { it.invocation.args[0] as UserEntity }

      val result = userService.getAndUpdateNomisUserOrCreate(username, jwt)

      assertThat(result.username).isEqualTo(nomisUser.username)
      assertThat(result.email).isEqualTo(nomisUser.primaryEmail)
      assertThat(result.nomisActiveCaseloadId).isEqualTo(nomisUser.activeCaseloadId)

      verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `getAndUpdateNomisUserOrCreate creates new nomis user`() {
      val nomisUser = buildNomisUserDetail(
        username = username.value,
      )
      every { nomisUserRolesService.getUserDetailsForMe(jwt) } returns nomisUser
      every { userRepository.findByUsernameAndAuthSource(username, AuthSource.NOMIS) } returns null
      every { userRepository.save(any()) } answers { it.invocation.args[0] as UserEntity }

      val result = userService.getAndUpdateNomisUserOrCreate(username, jwt)

      assertThat(result.username).isUpperCase.isEqualTo(nomisUser.username)
      assertThat(result.authSource).isEqualTo(AuthSource.NOMIS)
      assertThat(result.forename).isEqualTo(nomisUser.firstName)
      assertThat(result.middleNames).isNull()
      assertThat(result.surname).isEqualTo(nomisUser.lastName)
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

  @Nested
  inner class GetSystemUser {
    @Test
    fun `getSystemUser returns sas system user`() {
      val systemUser = buildUserEntity(
        username = "SAS_SYSTEM_USER",
        authSource = AuthSource.NONE,
      )
      every { userRepository.findByUsernameAndAuthSource(Username("SAS_SYSTEM_USER"), AuthSource.NONE) } returns systemUser

      val result = userService.getSystemUser()

      assertThat(result).isEqualTo(systemUser)
    }
  }
}
