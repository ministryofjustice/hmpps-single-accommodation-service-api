package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.HttpAuthService

@Service
class UserQueryService(
  private val httpAuthService: HttpAuthService,
  private val approvedPremisesAndDeliusCachingService: ApprovedPremisesAndDeliusCachingService,
) {
  fun getTeams() = approvedPremisesAndDeliusCachingService.getStaffDetail(httpAuthService.getUsername().value)?.teams?.map { team ->
    Team(
      code = team.code,
      name = team.name,
      ldu = Ldu(code = team.ldu.code, name = team.ldu.name),
      borough = team.borough?.let {
        Borough(
          code = it.code,
          description = it.description,
        )
      },
      startDate = team.startDate,
      endDate = team.endDate,
    )
  }
}
