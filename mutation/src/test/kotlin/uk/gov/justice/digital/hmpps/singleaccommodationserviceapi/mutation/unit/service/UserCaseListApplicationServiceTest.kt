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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseListType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserCaseListEntryRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.UserCaseListApplicationService
import java.util.UUID

@ExtendWith(MockKExtension::class)
class UserCaseListApplicationServiceTest {

  @RelaxedMockK
  private lateinit var userRepository: UserRepository

  @MockK
  private lateinit var caseRepository: CaseRepository

  @RelaxedMockK
  private lateinit var userCaseListEntryRepository: UserCaseListEntryRepository

  @InjectMockKs
  private lateinit var userCaseListApplicationService: UserCaseListApplicationService

  private val userId = UUID.randomUUID()

  @Test
  fun `de-duplicates crns before resolving case ids`() {
    every { caseRepository.findByCrns(listOf("CRN1", "CRN2")) } returns emptyList()

    userCaseListApplicationService.createUserCaseList(userId, CaseListType.CUSTOM, listOf("CRN1", "CRN2", "CRN1"))

    verify(exactly = 1) { caseRepository.findByCrns(listOf("CRN1", "CRN2")) }
  }

  @Test
  fun `locks the user row, then replaces the mappings`() {
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
}
