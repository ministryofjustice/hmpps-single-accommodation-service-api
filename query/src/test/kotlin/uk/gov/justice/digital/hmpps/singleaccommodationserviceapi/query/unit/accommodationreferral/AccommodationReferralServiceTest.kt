package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.accommodationreferral

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2Status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.CasService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factory.buildAccommodationReferralOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factory.buildReferralHistory
import java.time.Instant

@ExtendWith(MockKExtension::class)
class AccommodationReferralServiceTest {

  @MockK
  lateinit var orchestrationService: AccommodationReferralOrchestrationService

  @InjectMockKs
  lateinit var service: AccommodationReferralService

  private val crn = "X12345"

  @Nested
  inner class GetReferralHistory {

    @Test
    fun `should get referral history sorted by date descending`() {
      val olderDate = Instant.parse("2024-01-01T10:00:00Z")
      val middleDate = Instant.parse("2024-03-01T10:00:00Z")
      val newerDate = Instant.parse("2024-06-01T10:00:00Z")

      val orchestrationDto = buildAccommodationReferralOrchestrationDto(
        cas1Referrals = listOf(
          buildReferralHistory(CasService.CAS1, Cas1AssessmentStatus.COMPLETED, createdAt = olderDate),
        ),
        cas2Referrals = listOf(
          buildReferralHistory(CasService.CAS2, Cas2Status.PLACE_OFFERED, createdAt = newerDate),
        ),
        cas2v2Referrals = listOf(
          buildReferralHistory(CasService.CAS2v2, Cas2Status.AWAITING_DECISION, createdAt = middleDate),
        ),
        cas3Referrals = emptyList(),
      )

      every { orchestrationService.fetchAllReferralsAggregated(crn) } returns orchestrationDto

      val result = service.getReferralHistory(crn)

      assertThat(result).hasSize(3)
      assertThat(result[0].date).isEqualTo(newerDate)
      assertThat(result[1].date).isEqualTo(middleDate)
      assertThat(result[2].date).isEqualTo(olderDate)
    }

    @Test
    fun `should return empty list when no referrals exist`() {
      val orchestrationDto = buildAccommodationReferralOrchestrationDto(
        cas1Referrals = emptyList(),
        cas2Referrals = emptyList(),
        cas2v2Referrals = emptyList(),
        cas3Referrals = emptyList(),
      )

      every { orchestrationService.fetchAllReferralsAggregated(crn) } returns orchestrationDto

      val result = service.getReferralHistory(crn)

      assertThat(result).isEmpty()
    }

    @Test
    fun `should transform all referral types correctly`() {
      val orchestrationDto = buildAccommodationReferralOrchestrationDto()

      every { orchestrationService.fetchAllReferralsAggregated(crn) } returns orchestrationDto

      val result = service.getReferralHistory(crn)

      assertThat(result).hasSize(4)
      assertThat(result).containsExactlyInAnyOrderElementsOf(
        AccommodationReferralTransformer.transformReferrals(orchestrationDto),
      )
    }
  }
}
