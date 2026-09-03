package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.BulkRefreshCasesResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.CrnsRequiredException

@Service
class AdminBulkRefreshCasesService(
  private val caseRepository: CaseRepository,
  private val caseRefreshRequestService: CaseRefreshRequestService?,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  fun bulkRefreshCasesByCrn(crns: List<String>, dryRun: Boolean): ApiResponseDto<BulkRefreshCasesResultDto> {
    val refreshRequestService = caseRefreshRequestService
      ?: throw IllegalStateException("Case refresh request service is not enabled")

    val normalisedCrns = crns.map { it.trim().uppercase() }
      .filter(String::isNotEmpty)
      .distinct()
      .ifEmpty { throw CrnsRequiredException() }

    val casesByCrn = caseRepository.mapByCrns(normalisedCrns)
    val crnsNotFound = normalisedCrns.filterNot(casesByCrn::containsKey)
    val caseIds = casesByCrn.values.map { it.id }.distinct()

    if (!dryRun) {
      refreshRequestService.requestBulkRefresh(caseIds, resetAttempts = true)
    }

    return ApiResponseDto(
      data = BulkRefreshCasesResultDto(
        dryRun = dryRun,
        crnsRequested = normalisedCrns.size,
        casesFound = caseIds.size,
        refreshesRequested = if (dryRun) 0 else caseIds.size,
        crnsNotFound = crnsNotFound,
      ),
    ).also {
      log.info(
        "Bulk refresh by crn finished: {}. Refreshes (if any) are processed asynchronously by CaseRefreshWorker",
        it.data,
      )
    }
  }
}
