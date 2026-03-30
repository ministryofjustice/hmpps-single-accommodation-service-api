package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security

import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.nomisuserroles.NomisUserRolesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.ProbationIntegrationDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource as AuthSourceEntity

@Service
class UserService(
  private val httpAuthService: HttpAuthService,
  private val userRepository: UserRepository,
  private val probationIntegrationDeliusCachingService: ProbationIntegrationDeliusCachingService,
  private val nomisUserRolesService: NomisUserRolesService,
) {

  fun authorizeAndRetrieveUser(): UserEntity {
    val principal = httpAuthService.getPrincipalOrThrow(acceptableSources = listOf(AuthSource.DELIUS.source, AuthSource.NOMIS.source))
    return when (principal.authSource) {
      AuthSource.DELIUS -> {
        userRepository.findByUsernameAndAuthSource(
          username = principal.username,
          authSource = AuthSourceEntity.DELIUS,
        )!!
      }
      AuthSource.NOMIS -> {
        getAndUpdateNomisUserOrCreate(username = principal.username, httpAuthService.getJwt())
      }
      else -> throw RuntimeException("Unexpected auth_source: ${principal.authSource}")
    }
  }

  fun getExistingDeliusUserOrCreate(username: Username): UserEntity {
    val existingUser = userRepository.findByUsernameAndAuthSource(username, authSource = AuthSourceEntity.DELIUS)
    if (existingUser != null) {
      return existingUser
    }
    val staffUserDetails =
      probationIntegrationDeliusCachingService.getStaffDetail(username.value).orThrowNotFound("username" to username)
    val savedUser = userRepository.save(
      UserEntity(
        id = UUID.randomUUID(),
        username = username.value,
        authSource = AuthSourceEntity.DELIUS,
        name = staffUserDetails.name.deliusName(),
        deliusStaffCode = staffUserDetails.code,
        email = staffUserDetails.email,
        telephoneNumber = staffUserDetails.telephoneNumber,
        isActive = true,
        isEnabled = true,
        nomisStaffId = null,
        nomisAccountType = null,
        nomisActiveCaseloadId = null,
      ),
    )
    return savedUser
  }

  fun getAndUpdateNomisUserOrCreate(username: Username, jwt: Jwt): UserEntity {
    val nomisUserDetails =
      nomisUserRolesService.getUserDetailsForMe(jwt).orThrowNotFound("username" to username)
    val existingUser = userRepository.findByUsernameAndAuthSource(username = username, authSource = AuthSourceEntity.NOMIS)
    if (existingUser != null) {
      existingUser.email = nomisUserDetails.primaryEmail
      existingUser.nomisActiveCaseloadId = nomisUserDetails.activeCaseloadId
      return userRepository.save(existingUser)
    }
    return userRepository.save(
      UserEntity(
        id = UUID.randomUUID(),
        username = username.value,
        authSource = AuthSourceEntity.NOMIS,
        name = "${nomisUserDetails.firstName} ${nomisUserDetails.lastName}",
        email = nomisUserDetails.primaryEmail,
        isActive = nomisUserDetails.active,
        isEnabled = nomisUserDetails.enabled,
        nomisStaffId = nomisUserDetails.staffId,
        nomisAccountType = nomisUserDetails.accountType,
        nomisActiveCaseloadId = nomisUserDetails.activeCaseloadId,
        deliusStaffCode = null,
        telephoneNumber = null,
      ),
    )
  }

  fun findUserByUserId(id: UUID) = userRepository.findByIdOrNull(id)
}
