package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.security

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserContextService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserPrincipal
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.Username
import uk.gov.justice.hmpps.kotlin.auth.AuthSource as PrincipalAuthSource

@ExtendWith(value = [MockKExtension::class])
class UserContextServiceTest {

  @MockK
  lateinit var userService: UserService

  @InjectMockKs
  lateinit var userContextService: UserContextService

  @AfterEach
  fun tearDown() {
    SecurityContextHolder.clearContext()
  }

  @Nested
  inner class SetSasSystemUserAsSecurityContextUserPrincipal {
    @Test
    fun `setSasSystemUserAsSecurityContextUserPrincipal sets sas system user as security context principal`() {
      val systemUser = buildUserEntity(
        username = "SAS_SYSTEM_USER",
        authSource = AuthSource.NONE,
      )
      every { userService.getSystemUser() } returns systemUser

      userContextService.setUserContextAsSasSystemUser()

      val authentication = SecurityContextHolder.getContext().authentication
      val result = authentication!!.principal as UserPrincipal

      assertThat(result.sasUserId).isEqualTo(systemUser.id)
      assertThat(result.username).isEqualTo(Username(systemUser.username))
      assertThat(result.authSource).isEqualTo(PrincipalAuthSource.NONE)
    }
  }
}
