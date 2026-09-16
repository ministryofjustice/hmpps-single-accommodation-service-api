package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserCustomCaseListRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService

@Service
class CustomCaseListApplicationService(
  private val userService: UserService,
  private val userRepository: UserRepository,
  private val caseRepository: CaseRepository,
  private val userCustomCaseListRepository: UserCustomCaseListRepository,
  private val caseRefreshRequestService: CaseRefreshRequestService?,
) {
  @Transactional
  fun createCustomCaseList(crns: List<String>) {
    val user = userService.authorizeAndRetrieveUser()
    val distinctCrns = crns.distinct()

    val caseIds = caseRepository.findByCrns(distinctCrns).map { it.id }.distinct()

    // lock the user row so that concurrent requests for the same user wait for the current transaction to complete
    userRepository.findByIdForUpdate(user.id)

    userCustomCaseListRepository.deleteBySasUserId(user.id)
    userCustomCaseListRepository.insertAll(user.id, caseIds.toTypedArray())

    caseRefreshRequestService?.requestBulkRefresh(caseIds)
  }
}
