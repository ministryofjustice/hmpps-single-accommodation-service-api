package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.Team
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.HttpAuthService
import uk.gov.justice.hmpps.kotlin.auth.AuthSource

@Service
class UserQueryService(
  private val httpAuthService: HttpAuthService,
  private val approvedPremisesAndDeliusCachingService: ApprovedPremisesAndDeliusCachingService,
) {
  fun getTeams() = (getUserData() as DeliusUserDto).teams

  fun getUserData(): User {
    val principal = httpAuthService.getPrincipalOrThrow()
    return when (principal.authSource) {
      AuthSource.NOMIS -> NomisUserDto(
        username = principal.username.value,
      )

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
