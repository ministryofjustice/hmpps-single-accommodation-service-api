package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralWithdrawalDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralWithdrawalReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NoteCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildExternalReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ExternalReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ExternalReferralRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.ExternalReferralApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ARCHIVED_WITHDRAWAL_REASONS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.COMPLETED_WITHDRAWAL_REASONS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.REJECTED_WITHDRAWAL_REASONS
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import java.util.stream.Stream

@ExtendWith(MockKExtension::class)
class ExternalReferralApplicationServiceTest {

  @MockK
  lateinit var externalReferralRepository: ExternalReferralRepository

  @MockK
  lateinit var caseRepository: CaseRepository

  @MockK
  lateinit var userService: UserService

  @InjectMockKs
  lateinit var externalReferralApplicationService: ExternalReferralApplicationService

  private val crn = "X123456"
  private val submissionDate = LocalDate.of(2026, 1, 15)

  private val command = ExternalReferralCommand(
    submissionDate = submissionDate,
    referenceNumber = "REF-001",
    status = ExternalReferralStatus.SUBMITTED,
    organisationName = "Organisation name",
    website = "https://www.charity.org",
    submissionNote = "A submission note",
    email = "contact@example.com",
    phoneNumber = "01234567890",
  )

  @Nested
  inner class CreateExternalReferral {
    @BeforeEach
    fun setup() {
      every { externalReferralRepository.save(any()) } answers {
        firstArg<ExternalReferralEntity>().apply { createdAt = Instant.now() }
      }
    }

    @Test
    fun `creates an external referral for the given case and returns it in the response`() {
      val caseId = UUID.randomUUID()
      val case = buildCaseEntity(id = caseId)
      val user = buildUserEntity(forename = "Joe", surname = "Bloggs", username = "joe.bloggs")

      every { caseRepository.findByCrn(crn) } returns case
      every { userService.authorizeAndRetrieveUser() } returns user

      val result = externalReferralApplicationService.createExternalReferral(crn, command)

      assertThat(result.caseId).isEqualTo(caseId)
      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.status).isEqualTo(ExternalReferralStatus.SUBMITTED)
      assertThat(result.submission.referenceNumber).isEqualTo("REF-001")
      assertThat(result.submission.submissionDate).isEqualTo(submissionDate)
      assertThat(result.submission.organisationName).isEqualTo("Organisation name")
      assertThat(result.submission.createdBy).isEqualTo("Joe Bloggs")
      assertThat(result.submission.createdByUsername).isEqualTo("joe.bloggs")

      val savedEntity = slot<ExternalReferralEntity>()
      verify(exactly = 1) { externalReferralRepository.save(capture(savedEntity)) }
      assertThat(savedEntity.captured.caseId).isEqualTo(caseId)
      assertThat(savedEntity.captured.crn).isEqualTo(crn)
    }

    @Test
    fun `throws NotFoundException when the case does not exist for the given crn`() {
      every { caseRepository.findByCrn(crn) } returns null
      every { userService.authorizeAndRetrieveUser() } returns buildUserEntity()

      assertThrows<NotFoundException> {
        externalReferralApplicationService.createExternalReferral(crn, command)
      }

      verify(exactly = 0) { externalReferralRepository.save(any()) }
    }
  }

  @Nested
  inner class UpdateExternalReferral {
    private val referralId = UUID.randomUUID()
    private val createdByUserId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
      every { externalReferralRepository.save(any()) } answers { firstArg() }
    }

    @Test
    fun `updates an existing external referral and attributes it to the original creator`() {
      val referral = buildExternalReferralEntity(
        id = referralId,
        crn = crn,
        createdByUserId = createdByUserId,
      )
      val createdByUser = buildUserEntity(id = createdByUserId, forename = "Jane", surname = "Doe", username = "jane.doe")

      every { externalReferralRepository.findByIdAndCrn(referralId, crn) } returns referral
      every { userService.findUserByUserId(createdByUserId) } returns createdByUser

      val updateCommand = command.copy(referenceNumber = "REF-002", organisationName = "Updated organisation")

      val result = externalReferralApplicationService.updateExternalReferral(crn, referralId, updateCommand)

      assertThat(result.submission.referenceNumber).isEqualTo("REF-002")
      assertThat(result.submission.organisationName).isEqualTo("Updated organisation")
      assertThat(result.submission.createdBy).isEqualTo("Jane Doe")
      assertThat(result.submission.createdByUsername).isEqualTo("jane.doe")
      verify(exactly = 1) { externalReferralRepository.save(referral) }
      verify(exactly = 0) { userService.authorizeAndRetrieveUser() }
    }

    @Test
    fun `throws NotFoundException when the external referral does not exist for the given id and crn`() {
      every { externalReferralRepository.findByIdAndCrn(referralId, crn) } returns null

      assertThrows<NotFoundException> {
        externalReferralApplicationService.updateExternalReferral(crn, referralId, command)
      }

      verify(exactly = 0) { externalReferralRepository.save(any()) }
    }

    @Test
    fun `throws NotFoundException when the original creator user cannot be found`() {
      val referral = buildExternalReferralEntity(id = referralId, crn = crn, createdByUserId = createdByUserId)

      every { externalReferralRepository.findByIdAndCrn(referralId, crn) } returns referral
      every { userService.findUserByUserId(createdByUserId) } returns null

      assertThrows<NotFoundException> {
        externalReferralApplicationService.updateExternalReferral(crn, referralId, command)
      }

      verify(exactly = 0) { externalReferralRepository.save(any()) }
    }
  }

  @Nested
  inner class WithdrawalExternalReferral {
    private val referralId = UUID.randomUUID()
    private val createdByUserId = UUID.randomUUID()
    private val createdByUser = buildUserEntity(id = createdByUserId, forename = "Jane", surname = "Doe", username = "jane.doe")

    @BeforeEach
    fun setup() {
      every { externalReferralRepository.save(any()) } answers { firstArg() }
      every { userService.findUserByUserId(createdByUserId) } returns createdByUser
    }

    @Test
    fun `persists the withdrawal note and attributes the change to the original creator`() {
      every { externalReferralRepository.findByIdAndCrn(referralId, crn) } returns
        buildExternalReferralEntity(id = referralId, crn = crn, createdByUserId = createdByUserId)

      val result = externalReferralApplicationService.withdrawalExternalReferral(
        crn,
        referralId,
        ExternalReferralWithdrawalDto(
          withdrawalReason = ExternalReferralWithdrawalReason.PLACEMENT_COMPLETE,
          withdrawalNote = "A withdrawal note",
        ),
      )

      assertThat(result.submission.createdBy).isEqualTo("Jane Doe")
      assertThat(result.submission.createdByUsername).isEqualTo("jane.doe")
      verify(exactly = 0) { userService.authorizeAndRetrieveUser() }

      val savedEntity = slot<ExternalReferralEntity>()
      verify(exactly = 1) { externalReferralRepository.save(capture(savedEntity)) }
      assertThat(savedEntity.captured.withdrawalNote).isEqualTo("A withdrawal note")
      assertThat(savedEntity.captured.withdrawalReason?.name)
        .isEqualTo(ExternalReferralWithdrawalReason.PLACEMENT_COMPLETE.name)
    }

    @Test
    fun `throws NotFoundException when the external referral does not exist for the given id and crn`() {
      every { externalReferralRepository.findByIdAndCrn(referralId, crn) } returns null

      assertThrows<NotFoundException> {
        externalReferralApplicationService.withdrawalExternalReferral(
          crn,
          referralId,
          ExternalReferralWithdrawalDto(withdrawalReason = ExternalReferralWithdrawalReason.PLACEMENT_COMPLETE),
        )
      }

      verify(exactly = 0) { externalReferralRepository.save(any()) }
    }

    @Test
    fun `throws NotFoundException when the original creator user cannot be found`() {
      every { externalReferralRepository.findByIdAndCrn(referralId, crn) } returns
        buildExternalReferralEntity(id = referralId, crn = crn, createdByUserId = createdByUserId)
      every { userService.findUserByUserId(createdByUserId) } returns null

      assertThrows<NotFoundException> {
        externalReferralApplicationService.withdrawalExternalReferral(
          crn,
          referralId,
          ExternalReferralWithdrawalDto(withdrawalReason = ExternalReferralWithdrawalReason.PLACEMENT_COMPLETE),
        )
      }

      verify(exactly = 0) { externalReferralRepository.save(any()) }
    }
  }

  @ParameterizedTest
  @MethodSource("completedWithdrawalReasons")
  fun `withdraws a referral as COMPLETED for completed withdrawal reasons`(
    withdrawalReason: ExternalReferralWithdrawalReason,
  ) {
    assertWithdrawalResultsInStatus(withdrawalReason, ExternalReferralStatus.COMPLETED)
  }

  @ParameterizedTest
  @MethodSource("archivedWithdrawalReasons")
  fun `withdraws a referral as ARCHIVED for archived withdrawal reasons`(
    withdrawalReason: ExternalReferralWithdrawalReason,
  ) {
    assertWithdrawalResultsInStatus(withdrawalReason, ExternalReferralStatus.ARCHIVED)
  }

  @ParameterizedTest
  @MethodSource("rejectedWithdrawalReasons")
  fun `withdraws a referral as REJECTED for rejected withdrawal reasons`(
    withdrawalReason: ExternalReferralWithdrawalReason,
  ) {
    assertWithdrawalResultsInStatus(withdrawalReason, ExternalReferralStatus.REJECTED)
  }

  private fun assertWithdrawalResultsInStatus(
    withdrawalReason: ExternalReferralWithdrawalReason,
    expectedStatus: ExternalReferralStatus,
  ) {
    val referralId = UUID.randomUUID()
    val createdByUserId = UUID.randomUUID()
    every { externalReferralRepository.save(any()) } answers { firstArg() }
    every { userService.findUserByUserId(createdByUserId) } returns
      buildUserEntity(id = createdByUserId, forename = "Joe", surname = "Bloggs", username = "joe.bloggs")
    every { externalReferralRepository.findByIdAndCrn(referralId, crn) } returns
      buildExternalReferralEntity(id = referralId, crn = crn, createdByUserId = createdByUserId)

    val result = externalReferralApplicationService.withdrawalExternalReferral(
      crn,
      referralId,
      ExternalReferralWithdrawalDto(
        withdrawalReason = withdrawalReason,
        otherReason = "Some other reason".takeIf { withdrawalReason == ExternalReferralWithdrawalReason.ANOTHER_REASON },
      ),
    )

    assertThat(result.status).isEqualTo(expectedStatus)
  }

  @Nested
  inner class CreateExternalReferralNote {
    private val referralId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
      every { externalReferralRepository.save(any()) } answers { firstArg() }
    }

    @Test
    fun `adds a note to an existing external referral`() {
      val referral = buildExternalReferralEntity(id = referralId, crn = crn)
      every { externalReferralRepository.findByIdAndCrn(referralId, crn) } returns referral

      externalReferralApplicationService.createExternalReferralNote(crn, referralId, NoteCommand(note = "A note"))

      val savedEntity = slot<ExternalReferralEntity>()
      verify(exactly = 1) { externalReferralRepository.save(capture(savedEntity)) }
      assertThat(savedEntity.captured.notes.map { it.note }).containsExactly("A note")
    }

    @Test
    fun `throws NotFoundException when the external referral does not exist for the given id and crn`() {
      every { externalReferralRepository.findByIdAndCrn(referralId, crn) } returns null

      assertThrows<NotFoundException> {
        externalReferralApplicationService.createExternalReferralNote(crn, referralId, NoteCommand(note = "A note"))
      }

      verify(exactly = 0) { externalReferralRepository.save(any()) }
    }
  }

  private companion object {
    @JvmStatic
    fun completedWithdrawalReasons(): Stream<ExternalReferralWithdrawalReason> = COMPLETED_WITHDRAWAL_REASONS.stream()

    @JvmStatic
    fun archivedWithdrawalReasons(): Stream<ExternalReferralWithdrawalReason> = ARCHIVED_WITHDRAWAL_REASONS.stream()

    @JvmStatic
    fun rejectedWithdrawalReasons(): Stream<ExternalReferralWithdrawalReason> = REJECTED_WITHDRAWAL_REASONS.stream()
  }
}
