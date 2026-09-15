package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserCustomCaseListEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserCustomCaseListRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService

@Service
class CustomCaseListApplicationService(
  private val userService: UserService,
  private val caseApplicationService: CaseApplicationService,
  private val caseRepository: CaseRepository,
  private val userCustomCaseListRepository: UserCustomCaseListRepository,
  private val caseRefreshRequestService: CaseRefreshRequestService?,
) {
  @Transactional
  fun createCustomCaseList(crns: List<String>) {
    val user = userService.authorizeAndRetrieveUser()
    val distinctCrns = crns.distinct()

    caseApplicationService.createCases(distinctCrns.map { CrnToPrisonNumber(it, null) }, createAsBlankRecord = true)

    val caseIds = caseRepository.findByCrns(distinctCrns).map { it.id }.distinct()

    userCustomCaseListRepository.deleteBySasUserId(user.id)
    userCustomCaseListRepository.saveAll(
      caseIds.map { caseId -> UserCustomCaseListEntity(sasUserId = user.id, sasCaseId = caseId) },
    )

    caseRefreshRequestService?.requestBulkRefresh(caseIds)
  }
}
