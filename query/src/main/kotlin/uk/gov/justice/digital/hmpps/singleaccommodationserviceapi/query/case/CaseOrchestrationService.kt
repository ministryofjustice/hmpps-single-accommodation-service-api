package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PageMetadata
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.FULL_CASE_LIST
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CASE_LIST
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesandoasys.ApprovedPremisesAndOasysCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesandoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.CaseList
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SasAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierCachingService

@Service
class CaseOrchestrationService(
  val aggregatorService: AggregatorService,
  val sasAndDeliusCachingService: SasAndDeliusCachingService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val approvedPremisesAndOasysCachingService: ApprovedPremisesAndOasysCachingService,
  val tierCachingService: TierCachingService,
) {

  private val log = LoggerFactory.getLogger(javaClass)
  private val pageSize = 200L
  private val initialPage = 0L

  @Cacheable(FULL_CASE_LIST)
  fun getCaseList(username: String): OrchestrationResultDto<List<Case>> {
    log.info("Retrieving case list from Delius")
    val initialCall = mapOf(
      GET_CASE_LIST to {
        sasAndDeliusCachingService.getCaseList(
          username = username,
          page = initialPage,
          size = pageSize,
        )
      },
    )

    val initialResults = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = initialCall,
    )

    val initialResultSet = initialResults.standardCallsNoIterationResults!!

    val caseList = initialResultSet
      .getResult<CaseList>(GET_CASE_LIST)
      ?: CaseList(emptyList(), PageMetadata(0, 0, 0, 0))

    log.info(
      "Received {} cases, page {} of {}",
      caseList.cases.size,
      caseList.page.number + 1,
      caseList.page.totalPages,
    )

    val remainingPages = 1 until caseList.page.totalPages

    val (additionalCases, additionalFailures) =
      if (caseList.page.number + 1 < caseList.page.totalPages) {
        val additionalResults = aggregatorService.orchestrateAsyncCalls(
          standardCallsNoIteration = remainingPages.associate { nextPage ->
            (GET_CASE_LIST + nextPage) to {
              sasAndDeliusCachingService.getCaseList(
                username,
                page = nextPage,
                size = pageSize,
              )
            }
          },
        )

        val resultSet = additionalResults.standardCallsNoIterationResults!!

        val cases = remainingPages
          .mapNotNull { nextPage ->
            resultSet.getResult<CaseList>(GET_CASE_LIST + nextPage)?.cases
          }
          .flatten()

        cases to resultSet.getFailures()
      } else {
        emptyList<Case>() to emptyList()
      }

    val allFailures = initialResultSet.getFailures() + additionalFailures

    return OrchestrationResultDto(
      data = caseList.cases + additionalCases,
      upstreamFailures = allFailures,
    )
  }

  fun getCase(username: String, crn: String): OrchestrationResultDto<CaseOrchestrationDto> {
    val calls = mapOf(
      ApiCallKeys.GET_CASE to { sasAndDeliusCachingService.getCase(username, crn) },
      ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN to { corePersonRecordCachingService.getCorePersonRecordByCrn(crn) },
      ApiCallKeys.GET_ROSH_DETAIL to { approvedPremisesAndOasysCachingService.getRoshDetails(crn) },
      ApiCallKeys.GET_TIER to { tierCachingService.getTier(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val case = results.standardCallsNoIterationResults!!.getResult<Case>(ApiCallKeys.GET_CASE)
    val cpr = results.standardCallsNoIterationResults!!.getResult<CorePersonRecord>(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN)
    val roshDetails = results.standardCallsNoIterationResults!!.getResult<RoshDetails>(ApiCallKeys.GET_ROSH_DETAIL)
    val tier = results.standardCallsNoIterationResults!!.getResult<Tier>(ApiCallKeys.GET_TIER)

    return OrchestrationResultDto(
      data = CaseOrchestrationDto(
        crn = crn,
        cpr = cpr,
        roshDetails = roshDetails,
        tier = tier,
        case = case,
      ),
      upstreamFailures = results.standardCallsNoIterationResults!!.getFailures(),
    )
  }
}
