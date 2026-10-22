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
  fun getTeams() = (getUserData() as DeliusUserDto).teams

  fun getUserData(): UserDto {
    val principal = httpAuthService.getPrincipalOrThrow()
    return when (principal.authSource) {
      AuthSource.NOMIS -> {
        val jwt = httpAuthService.getJwt()
        val nomisUser = nomisUserRolesService.getUserDetailsForMe(jwt)

        NomisUserDto(
          username = principal.username.value,
          prisonCode = nomisUser?.activeCaseloadId,
        )
      }

      AuthSource.DELIUS -> {
        val user = approvedPremisesAndDeliusCachingService.getStaffDetail(principal.username.value)

        DeliusUserDto(
          username = principal.username.value,
          teams = user.teams.toTeamDtos(),
          probationAreaCode = user.probationArea.code,
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
