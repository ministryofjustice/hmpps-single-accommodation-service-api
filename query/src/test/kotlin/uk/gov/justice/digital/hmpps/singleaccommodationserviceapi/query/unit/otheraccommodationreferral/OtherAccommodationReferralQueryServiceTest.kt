package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.otheraccommodationreferral

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildLocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOtherAccommodationReferralEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OtherAccommodationReferralRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.otheraccommodationreferral.OtherAccommodationReferralQueryService
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockKExtension::class)
class OtherAccommodationReferralQueryServiceTest {

  @MockK
  lateinit var otherAccommodationReferralRepository: OtherAccommodationReferralRepository

  @MockK
  lateinit var userRepository: UserRepository

  @MockK
  lateinit var localAuthorityAreaRepository: LocalAuthorityAreaRepository

  @InjectMockKs
  lateinit var service: OtherAccommodationReferralQueryService

  private val id = UUID.randomUUID()
  private val caseId = UUID.randomUUID()
  private val crn = "X123456"
  private val localAuthorityAreaId = UUID.randomUUID()
  private val createdByUserId = UUID.randomUUID()

  @Nested
  inner class GetOtherAccommodationReferral {

    @Test
    fun `should return other accommodation referral when found by crn and id`() {
      val entity = buildOtherAccommodationReferralEntity(
        id = id,
        caseId = caseId,
        crn = crn,
        localAuthorityAreaId = localAuthorityAreaId,
        createdByUserId = createdByUserId,
        submissionDate = LocalDate.of(2026, 2, 20),
        referenceNumber = "REF-001",
        organisationName = "Organisation name",
        website = "https://www.charity.org",
        submissionNote = "A submission note",
      )
      val userEntity = buildUserEntity(
        id = createdByUserId,
        forename = "Joe",
        surname = "Bloggs",
        username = "JBLOGGS",
      )
      val localAuthorityAreaEntity = buildLocalAuthorityAreaEntity(
        id = localAuthorityAreaId,
        name = "Test Local Authority",
      )

      every { otherAccommodationReferralRepository.findByIdAndCrn(id, crn) } returns entity
      every { userRepository.findByIdOrNull(createdByUserId) } returns userEntity
      every { localAuthorityAreaRepository.findByIdOrNull(localAuthorityAreaId) } returns localAuthorityAreaEntity

      val result = service.getOtherAccommodationReferral(crn, id)

      assertThat(result.crn).isEqualTo(crn)
      assertThat(result.caseId).isEqualTo(caseId)
      assertThat(result.status).isEqualTo(OtherAccommodationReferralStatus.SUBMITTED)
      assertThat(result.submission).isNotNull
      val submission = result.submission
      assertThat(submission.id).isEqualTo(id)
      assertThat(submission.localAuthority.localAuthorityAreaId).isEqualTo(localAuthorityAreaId)
      assertThat(submission.localAuthority.localAuthorityAreaName).isEqualTo("Test Local Authority")
      assertThat(submission.referenceNumber).isEqualTo("REF-001")
      assertThat(submission.submissionDate).isEqualTo(LocalDate.of(2026, 2, 20))
      assertThat(submission.createdBy).isEqualTo("Joe Bloggs")
      assertThat(submission.createdByUsername).isEqualTo("JBLOGGS")
      assertThat(submission.organisationName).isEqualTo("Organisation name")
      assertThat(submission.website).isEqualTo("https://www.charity.org")
      assertThat(submission.submissionNote).isEqualTo("A submission note")
    }

    @Test
    fun `should throw NotFoundException when not found`() {
      every { otherAccommodationReferralRepository.findByIdAndCrn(id, crn) } returns null

      assertThatThrownBy { service.getOtherAccommodationReferral(crn, id) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("OtherAccommodationReferralEntity not found for [id=$id, crn=$crn]")
    }
  }
}
