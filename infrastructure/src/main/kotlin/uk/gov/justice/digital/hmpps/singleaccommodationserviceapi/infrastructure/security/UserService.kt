package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
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
) {
  fun getDeliusUserForRequest(): UserEntity {
    val username = httpAuthService.getDeliusPrincipalOrThrow().name
    return getExistingUserOrCreate(username)
  }

  fun getExistingUserOrCreate(username: String): UserEntity {
    val normalisedUsername = username.uppercase()
    val existingUser = userRepository.findByUsernameAndAuthSource(username = normalisedUsername, authSource = AuthSource.DELIUS)
    if (existingUser != null) {
      return existingUser
    }
    val staffUserDetails = probationIntegrationDeliusCachingService.getStaffDetail(normalisedUsername)
      ?: throw NotFoundException("Staff details for Delius user $normalisedUsername do not exist")

    val savedUser = userRepository.save(
      UserEntity(
        id = UUID.randomUUID(),
        username = normalisedUsername,
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

  fun findUserByUserId(id: UUID) = userRepository.findByIdOrNull(id)
}
