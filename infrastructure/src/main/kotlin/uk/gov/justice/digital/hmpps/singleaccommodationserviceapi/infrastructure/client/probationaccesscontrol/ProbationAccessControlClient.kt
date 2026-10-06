package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol

import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config.RestClientRetry

interface ProbationAccessControlClient {
  @PostExchange(value = "/user/{username}/access")
  fun getUserAccess(@PathVariable username: String, @RequestBody crns: List<String>): UserCaseAccess
}

@RestClientRetry
@Service
class ProbationAccessControlService(
  val probationAccessControlClient: ProbationAccessControlClient,
) {
  fun getUserAccess(username: String, crns: List<String>) = probationAccessControlClient.getUserAccess(username, crns)
}
