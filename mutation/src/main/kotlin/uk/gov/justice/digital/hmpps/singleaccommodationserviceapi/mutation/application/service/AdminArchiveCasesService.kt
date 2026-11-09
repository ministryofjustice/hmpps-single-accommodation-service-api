package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ArchiveCasesResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.CrnsRequiredException

@Service
class AdminArchiveCasesService(
  private val caseRepository: CaseRepository,
  private val caseRefreshRequestRepository: CaseRefreshRequestRepository,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  @Transactional
  fun archiveCasesByCrn(crns: List<String>, dryRun: Boolean): ApiResponseDto<ArchiveCasesResultDto> {
    val normalisedCrns = crns.map { it.trim().uppercase() }
      .filter(String::isNotEmpty)
      .distinct()
      .ifEmpty { throw CrnsRequiredException() }

    val casesByCrn = caseRepository.mapByCrns(normalisedCrns)
    val crnsNotFound = normalisedCrns.filterNot(casesByCrn::containsKey)
    val caseIds = casesByCrn.values.map { it.id }.distinct()

    var refreshRequestsDeleted = 0
    var casesArchived = 0
    if (!dryRun) {
      caseIds.forEach { caseId ->
        caseRefreshRequestRepository.findByCaseId(caseId)?.let { request ->
          caseRefreshRequestRepository.delete(request)
          refreshRequestsDeleted++
        }
        casesArchived += caseRepository.archiveById(caseId)
      }
    }

    return ApiResponseDto(
      data = ArchiveCasesResultDto(
        dryRun = dryRun,
        crnsRequested = normalisedCrns.size,
        casesFound = caseIds.size,
        casesArchived = casesArchived,
        refreshRequestsDeleted = refreshRequestsDeleted,
        crnsNotFound = crnsNotFound,
      ),
    ).also {
      log.info(
        "Archive cases by crn finished: dryRun={}, crnsRequested={}, casesFound={}, casesArchived={}, refreshRequestsDeleted={}, crnsNotFound={}",
        it.data.dryRun,
        it.data.crnsRequested,
        it.data.casesFound,
        it.data.casesArchived,
        it.data.refreshRequestsDeleted,
        it.data.crnsNotFound.size,
      )
    }
  }
}
