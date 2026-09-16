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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserCustomCaseListRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CustomCaseListApplicationService
import java.util.UUID

@ExtendWith(MockKExtension::class)
class CustomCaseListApplicationServiceTest {

  @RelaxedMockK
  private lateinit var userRepository: UserRepository

  @MockK
  private lateinit var caseRepository: CaseRepository

  @RelaxedMockK
  private lateinit var userCustomCaseListRepository: UserCustomCaseListRepository

  @RelaxedMockK
  private lateinit var caseRefreshRequestService: CaseRefreshRequestService

  @InjectMockKs
  private lateinit var customCaseListApplicationService: CustomCaseListApplicationService

  private val userId = UUID.randomUUID()

  @Test
  fun `de-duplicates crns before resolving case ids`() {
    every { caseRepository.findByCrns(listOf("CRN1", "CRN2")) } returns emptyList()

    customCaseListApplicationService.createCustomCaseList(userId, listOf("CRN1", "CRN2", "CRN1"))

    verify(exactly = 1) { caseRepository.findByCrns(listOf("CRN1", "CRN2")) }
  }

  @Test
  fun `locks the user row, then replaces the mappings`() {
    val caseIds = listOf(UUID.randomUUID(), UUID.randomUUID())
    every { caseRepository.findByCrns(any()) } returns caseIds.map { buildCaseEntity(id = it) }

    customCaseListApplicationService.createCustomCaseList(userId, listOf("CRN1", "CRN2"))

    val insertedCaseIds = slot<Array<UUID>>()
    verifyOrder {
      userRepository.findByIdForUpdate(userId)
      userCustomCaseListRepository.deleteBySasUserId(userId)
      userCustomCaseListRepository.insertAll(userId, capture(insertedCaseIds))
    }
    assertThat(insertedCaseIds.captured).containsExactlyInAnyOrderElementsOf(caseIds)
  }

  @Test
  fun `requests a bulk refresh for the resolved case ids`() {
    val caseIds = listOf(UUID.randomUUID(), UUID.randomUUID())
    every { caseRepository.findByCrns(any()) } returns caseIds.map { buildCaseEntity(id = it) }

    customCaseListApplicationService.createCustomCaseList(userId, listOf("CRN1", "CRN2"))

    verify(exactly = 1) { caseRefreshRequestService.requestBulkRefresh(caseIds) }
  }

  @Test
  fun `completes without requesting a refresh when the case refresh mechanism is not enabled`() {
    val caseIds = listOf(UUID.randomUUID())
    val service = CustomCaseListApplicationService(
      userRepository = userRepository,
      caseRepository = caseRepository,
      userCustomCaseListRepository = userCustomCaseListRepository,
      caseRefreshRequestService = null,
    )
    every { caseRepository.findByCrns(any()) } returns caseIds.map { buildCaseEntity(id = it) }

    service.createCustomCaseList(userId, listOf("CRN1"))

    verify(exactly = 1) { userCustomCaseListRepository.deleteBySasUserId(userId) }
    verify(exactly = 1) { userCustomCaseListRepository.insertAll(userId, any()) }
    verify(exactly = 0) { caseRefreshRequestService.requestBulkRefresh(any()) }
  }
}
