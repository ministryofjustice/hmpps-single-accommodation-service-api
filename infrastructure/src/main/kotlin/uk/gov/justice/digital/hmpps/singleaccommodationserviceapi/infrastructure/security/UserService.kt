package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.nomisuserroles.NomisUserRolesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.ProbationIntegrationDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import java.util.UUID
import kotlin.String

@Service
class UserService(
  private val httpAuthService: HttpAuthService,
  private val userRepository: UserRepository,
  private val probationIntegrationDeliusCachingService: ProbationIntegrationDeliusCachingService,
  private val nomisUserRolesService: NomisUserRolesService,
) {
  fun getUserForRequest(): UserEntity {
    val (username, authSource) = httpAuthService.getPrincipalOrThrow(acceptableSources = listOf(AuthSource.DELIUS.authSource, AuthSource.NOMIS.authSource))
    val normalisedUsername = username.uppercase()
    return when (AuthSource.fromString(authSource)) {
      AuthSource.DELIUS -> getExistingDeliusUserOrCreate(username = normalisedUsername)
      AuthSource.NOMIS -> getAndUpdateNomisUserOrCreate(username = normalisedUsername)
    }
  }

  fun getExistingDeliusUserOrCreate(username: String): UserEntity {
    val existingUser = userRepository.findByUsernameAndAuthSource(username, authSource = AuthSource.DELIUS)
    if (existingUser != null) {
      return existingUser
    }
    val staffUserDetails = probationIntegrationDeliusCachingService.getStaffDetail(username)
      ?: throw NotFoundException("Staff details for Delius user $username do not exist")
    val savedUser = userRepository.save(
      UserEntity(
        id = UUID.randomUUID(),
        username = username,
        authSource = AuthSource.DELIUS,
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

  fun getAndUpdateNomisUserOrCreate(username: String): UserEntity {
    val nomisUserDetails = nomisUserRolesService.getUserDetailsForMe()
      ?: throw NotFoundException("User details for Nomis user $username do not exist")
    val existingUser = userRepository.findByUsernameAndAuthSource(username = username, authSource = AuthSource.NOMIS)
    if (existingUser != null) {
      if (
        existingUser.email != nomisUserDetails.primaryEmail ||
        existingUser.nomisActiveCaseloadId != nomisUserDetails.activeCaseloadId
      ) {
        existingUser.email = nomisUserDetails.primaryEmail
        existingUser.nomisActiveCaseloadId = nomisUserDetails.activeCaseloadId

        return userRepository.save(existingUser)
      }

      return existingUser
    }
    return userRepository.save(
      UserEntity(
        id = UUID.randomUUID(),
        username = username,
        authSource = AuthSource.NOMIS,
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
