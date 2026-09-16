package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserCustomCaseListRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import java.util.UUID

@Service
class CustomCaseListApplicationService(
  private val userRepository: UserRepository,
  private val caseRepository: CaseRepository,
  private val userCustomCaseListRepository: UserCustomCaseListRepository,
  private val caseRefreshRequestService: CaseRefreshRequestService?,
) {
  @Transactional
  fun createCustomCaseList(userId: UUID, crns: List<String>) {
    val distinctCrns = crns.distinct()

    val caseIds = caseRepository.findByCrns(distinctCrns).map { it.id }.distinct()

    // lock the user row so that concurrent requests for the same user wait for the current transaction to complete
    userRepository.findByIdForUpdate(userId)

    userCustomCaseListRepository.deleteBySasUserId(userId)
    userCustomCaseListRepository.insertAll(userId, caseIds.toTypedArray())

    caseRefreshRequestService?.requestBulkRefresh(caseIds)
  }
}
