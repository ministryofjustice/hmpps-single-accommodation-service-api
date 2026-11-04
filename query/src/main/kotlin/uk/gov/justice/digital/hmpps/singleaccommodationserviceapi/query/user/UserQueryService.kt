package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.Team
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.nomisuserroles.NomisUserRolesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.TeamStaffMember
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.HttpAuthService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto
import uk.gov.justice.hmpps.kotlin.auth.AuthSource

@Service
class UserQueryService(
  private val httpAuthService: HttpAuthService,
  private val approvedPremisesAndDeliusCachingService: ApprovedPremisesAndDeliusCachingService,
  private val nomisUserRolesService: NomisUserRolesService,
  private val userOrchestrationService: UserOrchestrationService,
) {
  fun getTeams() = getUserData().deliusUserDetails?.teams

  fun getUserTeamMembers(): ApiResponseDto<List<TeamMemberDto>> {
    val principal = httpAuthService.getPrincipalOrThrow()
    val teamCodes = getTeams()?.map { it.code }.orEmpty()
    if (teamCodes.isEmpty()) return toApiResponseDto(data = emptyList())

    val orchestrationResult = userOrchestrationService.getStaffForTeams(teamCodes)

    return toApiResponseDto(
      data = orchestrationResult.data.toTeamMemberDtos(currentUsername = principal.username.value),
      upstreamFailures = orchestrationResult.upstreamFailures,
    )
  }

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

  private fun List<TeamStaffMember>.toTeamMemberDtos(currentUsername: String) = this
    .mapNotNull { staff ->
      staff.username?.takeIf { it.isNotBlank() }?.let { username ->
        TeamMemberDto(username = username, forename = staff.name.forename, surname = staff.name.surname)
      }
    }
    .filterNot { it.username.equals(currentUsername, ignoreCase = true) }
    .distinctBy { it.username }
    .sortedWith(
      compareBy<TeamMemberDto, String>(String.CASE_INSENSITIVE_ORDER) { it.forename }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.surname },
    )
}
