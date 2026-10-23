package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.Team
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.nomisuserroles.NomisUserRolesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.HttpAuthService
import uk.gov.justice.hmpps.kotlin.auth.AuthSource

@Service
class UserQueryService(
  private val httpAuthService: HttpAuthService,
  private val approvedPremisesAndDeliusCachingService: ApprovedPremisesAndDeliusCachingService,
  private val nomisUserRolesService: NomisUserRolesService,
) {
  fun getTeams() = getUserData().deliusUserDetails?.teams

  fun getUserData(): UserDto {
    val principal = httpAuthService.getPrincipalOrThrow()
    return when (principal.authSource) {
      AuthSource.NOMIS -> {
        val nomisUser = nomisUserRolesService.getNomisUserDetail(principal.username)

        UserDto(
          userType = UserType.NOMIS,
          username = principal.username.value,
          nomisUserDetails = NomisUserDetailsDto(prisonCode = nomisUser.activeCaseloadId ?: "Unknown"),
        )
      }

      AuthSource.DELIUS -> {
        val user = approvedPremisesAndDeliusCachingService.getStaffDetail(principal.username.value)

        UserDto(
          userType = UserType.DELIUS,
          username = principal.username.value,
          deliusUserDetails = DeliusUserDetailsDto(
            teams = user.teams.toTeamDtos(),
            probationAreaCode = user.probationArea.code,
          ),
        )
      }

      AuthSource.NONE,
      AuthSource.AUTH,
      -> error("Could not find DELIUS or NOMIS auth source")
    }
  }

  private fun List<Team>.toTeamDtos() = this.map { team ->
    TeamDto(name = team.name, code = team.code)
  }
}
