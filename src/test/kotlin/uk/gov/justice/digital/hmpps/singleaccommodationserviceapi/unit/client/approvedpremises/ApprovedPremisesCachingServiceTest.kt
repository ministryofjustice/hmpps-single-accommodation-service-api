package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.client.approvedpremises

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.CasReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.CasType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.Referral
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.Cas2v2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.generated.TemporaryAccommodationAssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.ApprovedPremisesClient
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class)
class ApprovedPremisesCachingServiceTest {

  @MockK
  lateinit var client: ApprovedPremisesClient

  private lateinit var service: ApprovedPremisesCachingService

  @BeforeEach
  fun setup() {
    service = ApprovedPremisesCachingService(client)
  }

  @Test
  fun `getCas1Referral delegates and maps`() {
    val crn = "X12345"
    val id = UUID.randomUUID()
    val createdAt = Instant.now()
    every { client.getCas1Referral(crn) } returns listOf(
      Cas1ReferralHistory(
        type = ServiceType.CAS1,
        id = id,
        applicationId = UUID.randomUUID(),
        status = Cas1AssessmentStatus.completed,
        createdAt = createdAt,
      ),
    )

    val result = service.getCas1Referral(crn)

    assertThat(result).containsExactly(
      Referral(id = id, type = CasType.CAS1, status = CasReferralStatus.ACCEPTED, date = createdAt),
    )
    verify(exactly = 1) { client.getCas1Referral(crn) }
  }

  @Test
  fun `getCas2Referral delegates and maps`() {
    val crn = "X12345"
    val id = UUID.randomUUID()
    val createdAt = Instant.now()
    every { client.getCas2Referral(crn) } returns listOf(
      Cas2ReferralHistory(
        type = ServiceType.CAS2,
        id = id,
        applicationId = UUID.randomUUID(),
        status = "Offer accepted",
        createdAt = createdAt,
      ),
    )

    val result = service.getCas2Referral(crn)

    assertThat(result).containsExactly(
      Referral(id = id, type = CasType.CAS2, status = CasReferralStatus.ACCEPTED, date = createdAt),
    )
    verify(exactly = 1) { client.getCas2Referral(crn) }
  }

  @Test
  fun `getCas2v2Referral delegates and maps`() {
    val crn = "X12345"
    val id = UUID.randomUUID()
    val createdAt = Instant.now()
    every { client.getCas2v2Referral(crn) } returns listOf(
      Cas2v2ReferralHistory(
        type = ServiceType.CAS2v2,
        id = id,
        applicationId = UUID.randomUUID(),
        status = "Referral cancelled",
        createdAt = createdAt,
      ),
    )

    val result = service.getCas2v2Referral(crn)

    assertThat(result).containsExactly(
      Referral(id = id, type = CasType.CAS2v2, status = CasReferralStatus.REJECTED, date = createdAt),
    )
    verify(exactly = 1) { client.getCas2v2Referral(crn) }
  }

  @Test
  fun `getCas3Referral delegates and maps`() {
    val crn = "X12345"
    val id = UUID.randomUUID()
    val createdAt = Instant.now()
    every { client.getCas3Referral(crn) } returns listOf(
      Cas3ReferralHistory(
        type = ServiceType.CAS3,
        id = id,
        applicationId = UUID.randomUUID(),
        status = TemporaryAccommodationAssessmentStatus.inReview,
        createdAt = createdAt,
      ),
    )

    val result = service.getCas3Referral(crn)

    assertThat(result).containsExactly(
      Referral(id = id, type = CasType.CAS3, status = CasReferralStatus.PENDING, date = createdAt),
    )
    verify(exactly = 1) { client.getCas3Referral(crn) }
  }
}
