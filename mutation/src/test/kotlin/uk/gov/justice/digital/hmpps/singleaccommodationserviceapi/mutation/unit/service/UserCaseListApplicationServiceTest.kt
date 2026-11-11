package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseListType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserCaseListEntryRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.UserCaseListApplicationService
import java.time.Clock
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class)
class UserCaseListApplicationServiceTest {

  @RelaxedMockK
  private lateinit var userRepository: UserRepository

  @MockK
  private lateinit var caseRepository: CaseRepository

  @RelaxedMockK
  private lateinit var userCaseListEntryRepository: UserCaseListEntryRepository

  @MockK
  private lateinit var clock: Clock

  @InjectMockKs
  private lateinit var userCaseListApplicationService: UserCaseListApplicationService

  private val userId = UUID.randomUUID()
  private val crn = "X123456"
  private val now = Instant.parse("2026-11-09T10:00:00Z")

  @Test
  fun `should de-duplicate crns before resolving case ids when creating a user case list`() {
    every { caseRepository.findByCrns(listOf("CRN1", "CRN2")) } returns emptyList()

    userCaseListApplicationService.createUserCaseList(userId, CaseListType.CUSTOM, listOf("CRN1", "CRN2", "CRN1"))

    verify(exactly = 1) { caseRepository.findByCrns(listOf("CRN1", "CRN2")) }
  }

  @Test
  fun `should lock the user row then replace existing entries when creating a user case list`() {
    val caseIds = listOf(UUID.randomUUID(), UUID.randomUUID())
    every { caseRepository.findByCrns(any()) } returns caseIds.map { buildCaseEntity(id = it) }

    userCaseListApplicationService.createUserCaseList(userId, CaseListType.CUSTOM, listOf("CRN1", "CRN2"))

    val insertedCaseIds = slot<Array<UUID>>()
    verifyOrder {
      userRepository.findByIdForUpdate(userId)
      userCaseListEntryRepository.deleteBySasUserIdAndListType(userId, CaseListType.CUSTOM)
      userCaseListEntryRepository.insertAll(userId, CaseListType.CUSTOM, capture(insertedCaseIds))
    }
    assertThat(insertedCaseIds.captured).containsExactlyInAnyOrderElementsOf(caseIds)
  }

  @Test
  fun `should lock the user row, insert or update the entry, then remove entries beyond the limit when adding to search history`() {
    val case = buildCaseEntity()
    every { caseRepository.findByCrn(crn) } returns case
    every { clock.instant() } returns now

    userCaseListApplicationService.addOrUpdateCaseToSearchHistory(userId, crn)

    verifyOrder {
      userRepository.findByIdForUpdate(userId)
      userCaseListEntryRepository.insertOrUpdate(userId, CaseListType.SEARCH_HISTORY, case.id, now)
      userCaseListEntryRepository.deleteOldestBeyondLimit(
        userId,
        CaseListType.SEARCH_HISTORY,
        UserCaseListApplicationService.MAX_SEARCH_HISTORY_ENTRIES,
      )
    }
  }

  @Test
  fun `should throw NotFoundException and leave search history unchanged when the case does not exist`() {
    every { caseRepository.findByCrn(crn) } returns null

    assertThatThrownBy { userCaseListApplicationService.addOrUpdateCaseToSearchHistory(userId, crn) }
      .isInstanceOf(NotFoundException::class.java)

    verify(exactly = 0) { userCaseListEntryRepository.insertOrUpdate(any(), any(), any(), any()) }
    verify(exactly = 0) { userCaseListEntryRepository.deleteOldestBeyondLimit(any(), any(), any()) }
  }
}
