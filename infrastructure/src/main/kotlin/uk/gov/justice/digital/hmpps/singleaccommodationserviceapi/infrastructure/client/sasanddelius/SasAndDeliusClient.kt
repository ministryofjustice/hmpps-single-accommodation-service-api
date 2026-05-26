package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import java.util.concurrent.ExecutorService
import java.util.concurrent.Semaphore

interface SasAndDeliusClient {

  @GetExchange(value = "/case-list/{username}")
  fun getCaseList(@PathVariable username: String, @RequestParam size: Int, @RequestParam page: Int): CaseList

  @GetExchange(value = "/case/{username}/{crn}")
  fun getCase(@PathVariable username: String, @PathVariable crn: String): Case
}

@Service
class SasAndDeliusCachingService(
  private val sasAndDeliusClient: SasAndDeliusClient,
  private val executor: ExecutorService,
) {

  private val log = LoggerFactory.getLogger(javaClass)
  private val sasAndDeliusSemaphore = Semaphore(25)

  @Cacheable(ApiCallKeys.GET_CASE_LIST)
  fun getCaseList(
    username: String,
    size: Int = 25,
    page: Int = 0,
  ): List<Case> {
    val initialResult = sasAndDeliusClient.getCaseList(username, size, page)
    val totalPages = initialResult.page.totalPages.toInt()

    log.info("Received {} cases, page {} of {}", initialResult.cases.size, page + 1, totalPages)

    if (totalPages <= 1) {
      return initialResult.cases
    }

    val futures = (1 until totalPages).map { nextPage ->
      executor.submit<List<Case>> {
        sasAndDeliusSemaphore.acquire()

        try {
          log.info(
            "getting page {} of {} on thread {}",
            nextPage + 1,
            totalPages,
            Thread.currentThread().name,
          )
          sasAndDeliusClient.getCaseList(username, size, nextPage).cases
        } finally {
          sasAndDeliusSemaphore.release()
        }
      }
    }

    val additionalResults =
      futures.flatMap { it.get() }

    log.info("Successfully received {} additional cases", additionalResults.size)

    return initialResult.cases + additionalResults
  }

  @Cacheable(ApiCallKeys.GET_CASE, sync = true)
  fun getCase(username: String, crn: String) = sasAndDeliusClient.getCase(username, crn)
}
