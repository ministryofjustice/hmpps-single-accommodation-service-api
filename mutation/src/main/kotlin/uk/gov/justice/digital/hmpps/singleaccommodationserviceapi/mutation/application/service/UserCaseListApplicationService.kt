package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseListType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserCaseListEntryRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import java.time.Clock
import java.util.UUID

@Service
class UserCaseListApplicationService(
  private val userRepository: UserRepository,
  private val caseRepository: CaseRepository,
  private val userCaseListEntryRepository: UserCaseListEntryRepository,
  private val clock: Clock,
) {
  companion object {
    const val MAX_SEARCH_HISTORY_ENTRIES = 10
  }

  @Transactional
  fun createUserCaseList(userId: UUID, listType: CaseListType, crns: List<String>) {
    val distinctCrns = crns.distinct()

    val caseIds = caseRepository.findByCrns(distinctCrns).map { it.id }.distinct()

    // lock the user row so that concurrent requests for the same user wait for the current transaction to complete
    userRepository.findByIdForUpdate(userId)

    userCaseListEntryRepository.deleteBySasUserIdAndListType(userId, listType)
    userCaseListEntryRepository.insertAll(userId, listType, caseIds.toTypedArray())
  }

  @Transactional
  fun addOrUpdateCaseToSearchHistory(userId: UUID, crn: String) {
    val caseId = caseRepository.findByCrn(crn)?.id ?: throw NotFoundException("Case", mapOf("crn" to crn))

    // lock the user row so that concurrent searches for the same user cannot exceed the history limit
    userRepository.findByIdForUpdate(userId)

    userCaseListEntryRepository.insertOrUpdate(userId, CaseListType.SEARCH_HISTORY, caseId, clock.instant())
    userCaseListEntryRepository.deleteOldestBeyondLimit(userId, CaseListType.SEARCH_HISTORY, MAX_SEARCH_HISTORY_ENTRIES)
  }
}
