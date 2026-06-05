package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security

import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.StaffDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.nomisuserroles.NomisUserRolesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource as AuthSourceEntity

@Service
class UserService(
  private val httpAuthService: HttpAuthService,
  private val userRepository: UserRepository,
  private val approvedPremisesAndDeliusCachingService: ApprovedPremisesAndDeliusCachingService,
  private val nomisUserRolesService: NomisUserRolesService,
) {

  private fun getPrincipal() = httpAuthService.getPrincipalOrThrow(acceptableSources = listOf(AuthSource.DELIUS.source, AuthSource.NOMIS.source))

  fun getUsername() = getPrincipal().username

  fun authorizeAndRetrieveUser(): UserEntity {
    val principal = getPrincipal()
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

  fun getAndUpsertDeliusUser(username: Username): UserEntity {
    val staffDetail =
      approvedPremisesAndDeliusCachingService.getStaffDetail(username.value).orThrowNotFound("username" to username)

    val user =
      userRepository.findByUsernameAndAuthSource(username, authSource = AuthSourceEntity.DELIUS)?.update(staffDetail)
        ?: createDeliusUser(username, staffDetail)

    return userRepository.save(user)
  }

  private fun UserEntity.update(staffDetail: StaffDetail) = this.apply {
    forename = staffDetail.name.forename
    middleNames = staffDetail.name.middleName
    surname = staffDetail.name.surname
    deliusStaffCode = staffDetail.code
    email = staffDetail.email
    telephoneNumber = staffDetail.telephoneNumber
    isActive = staffDetail.active
  }

  private fun createDeliusUser(username: Username, staffDetail: StaffDetail) = UserEntity(
    id = UUID.randomUUID(),
    username = username.value,
    authSource = AuthSourceEntity.DELIUS,
    forename = staffDetail.name.forename,
    middleNames = staffDetail.name.middleName,
    surname = staffDetail.name.surname,
    deliusStaffCode = staffDetail.code,
    email = staffDetail.email,
    telephoneNumber = staffDetail.telephoneNumber,
    isActive = staffDetail.active,
    isEnabled = null,
    nomisStaffId = null,
    nomisAccountType = null,
    nomisActiveCaseloadId = null,
  )

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
        forename = nomisUserDetails.firstName,
        middleNames = null, // not provided in API response
        surname = nomisUserDetails.lastName,
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
