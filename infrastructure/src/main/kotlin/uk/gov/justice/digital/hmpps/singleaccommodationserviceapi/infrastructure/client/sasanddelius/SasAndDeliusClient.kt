package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CASE_LIST

interface SasAndDeliusClient {
  @GetExchange(value = "/case-list/{username}")
  fun getCaseList(@PathVariable username: String, @RequestParam page: Long, @RequestParam size: Long): CaseList

  @GetExchange(value = "/case/{username}/{crn}")
  fun getCase(@PathVariable username: String, @PathVariable crn: String): Case
}

@Service
class SasAndDeliusCachingService(
  val sasAndDeliusClient: SasAndDeliusClient,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  @Cacheable(GET_CASE_LIST)
  fun getCaseList(
    username: String,
    page: Long,
    size: Long,
  ): CaseList {
    log.debug("Calling getCaseList for username: $username, size: $size, page: $page")
    return sasAndDeliusClient.getCaseList(username = username, page = page, size = size)
  }

  @Cacheable(ApiCallKeys.GET_CASE, sync = true)
  fun getCase(username: String, crn: String) = sasAndDeliusClient.getCase(username, crn)
}
