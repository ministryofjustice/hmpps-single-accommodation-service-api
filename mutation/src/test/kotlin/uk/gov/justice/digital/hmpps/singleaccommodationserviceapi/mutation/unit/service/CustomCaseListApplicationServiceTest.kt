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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserCustomCaseListRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CrnToPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CustomCaseListApplicationService
import java.util.UUID

@ExtendWith(MockKExtension::class)
class CustomCaseListApplicationServiceTest {

  @MockK
  private lateinit var userService: UserService

  @RelaxedMockK
  private lateinit var userRepository: UserRepository

  @RelaxedMockK
  private lateinit var caseApplicationService: CaseApplicationService

  @MockK
  private lateinit var caseRepository: CaseRepository

  @RelaxedMockK
  private lateinit var userCustomCaseListRepository: UserCustomCaseListRepository

  @RelaxedMockK
  private lateinit var caseRefreshRequestService: CaseRefreshRequestService

  @InjectMockKs
  private lateinit var customCaseListApplicationService: CustomCaseListApplicationService

  private val user = buildUserEntity()

  @Test
  fun `de-duplicates crns before creating cases`() {
    every { userService.authorizeAndRetrieveUser() } returns user
    every { caseRepository.findByCrns(listOf("CRN1", "CRN2")) } returns emptyList()

    customCaseListApplicationService.createCustomCaseList(listOf("CRN1", "CRN2", "CRN1"))

    verify(exactly = 1) {
      caseApplicationService.createCases(
        listOf(
          CrnToPrisonNumber(crn = "CRN1", prisonNumber = null),
          CrnToPrisonNumber(crn = "CRN2", prisonNumber = null),
        ),
        createAsBlankRecord = true,
      )
    }
  }

  @Test
  fun `creates cases, locks the user row then replaces the mappings`() {
    val caseIds = listOf(UUID.randomUUID(), UUID.randomUUID())
    every { userService.authorizeAndRetrieveUser() } returns user
    every { caseRepository.findByCrns(any()) } returns caseIds.map { buildCaseEntity(id = it) }

    customCaseListApplicationService.createCustomCaseList(listOf("CRN1", "CRN2"))

    val insertedCaseIds = slot<Array<UUID>>()
    verifyOrder {
      caseApplicationService.createCases(any(), createAsBlankRecord = true)
      userRepository.findByIdForUpdate(user.id)
      userCustomCaseListRepository.deleteBySasUserId(user.id)
      userCustomCaseListRepository.insertAll(user.id, capture(insertedCaseIds))
    }
    assertThat(insertedCaseIds.captured).containsExactlyInAnyOrderElementsOf(caseIds)
  }

  @Test
  fun `requests a bulk refresh for the resolved case ids`() {
    val caseIds = listOf(UUID.randomUUID(), UUID.randomUUID())
    every { userService.authorizeAndRetrieveUser() } returns user
    every { caseRepository.findByCrns(any()) } returns caseIds.map { buildCaseEntity(id = it) }

    customCaseListApplicationService.createCustomCaseList(listOf("CRN1", "CRN2"))

    verify(exactly = 1) { caseRefreshRequestService.requestBulkRefresh(caseIds) }
  }

  @Test
  fun `completes without requesting a refresh when the case refresh mechanism is not enabled`() {
    val caseIds = listOf(UUID.randomUUID())
    val service = CustomCaseListApplicationService(
      userService = userService,
      userRepository = userRepository,
      caseApplicationService = caseApplicationService,
      caseRepository = caseRepository,
      userCustomCaseListRepository = userCustomCaseListRepository,
      caseRefreshRequestService = null,
    )
    every { userService.authorizeAndRetrieveUser() } returns user
    every { caseRepository.findByCrns(any()) } returns caseIds.map { buildCaseEntity(id = it) }

    service.createCustomCaseList(listOf("CRN1"))

    verify(exactly = 1) { userCustomCaseListRepository.deleteBySasUserId(user.id) }
    verify(exactly = 1) { userCustomCaseListRepository.insertAll(user.id, any()) }
    verify(exactly = 0) { caseRefreshRequestService.requestBulkRefresh(any()) }
  }
}
