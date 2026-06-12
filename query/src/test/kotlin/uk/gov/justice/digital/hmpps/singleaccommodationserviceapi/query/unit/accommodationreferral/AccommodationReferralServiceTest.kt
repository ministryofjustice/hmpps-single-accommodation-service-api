package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.accommodationreferral

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2ReferralHistory.Cas2Status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDeliusUserDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildAccommodationReferralOrchestrationDto
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
class AccommodationReferralServiceTest {

  @MockK
  lateinit var orchestrationService: AccommodationReferralOrchestrationService

  @MockK
  lateinit var dutyToReferQueryService: DutyToReferQueryService

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
          buildReferralHistory(Cas1AssessmentStatus.COMPLETED, createdAt = olderDate, referredBy = buildDeliusUserDto()),
        ),
        cas2HdcReferrals = listOf(
          buildReferralHistory(Cas2Status.PLACE_OFFERED, createdAt = newerDate, referredBy = buildDeliusUserDto()),
        ),
        cas2Referrals = listOf(buildReferralHistory(Cas2Status.AWAITING_DECISION, createdAt = middleDate, referredBy = buildDeliusUserDto())),
        cas3Referrals = emptyList(),
      )
      val dtrSubmissionDate = LocalDate.of(2025, 5, 1)
      val dutyToReferDto = buildDutyToReferDto(crn = crn, submissionDate = dtrSubmissionDate)

      every { orchestrationService.fetchAllReferralsAggregated(crn) } returns OrchestrationResultDto(data = orchestrationDto)
      every { dutyToReferQueryService.getDutyToReferHistory(crn) } returns listOf(dutyToReferDto)

      val result = service.getReferralHistory(crn)

      assertThat(result.data).hasSize(4)
      assertThat(result.data[0].date).isEqualTo(dtrSubmissionDate.atStartOfDay().atOffset(ZoneOffset.UTC).toInstant())
      assertThat(result.data[1].date).isEqualTo(newerDate)
      assertThat(result.data[2].date).isEqualTo(middleDate)
      assertThat(result.data[3].date).isEqualTo(olderDate)
    }

    @Test
    fun `should return empty list when no referrals exist`() {
      val orchestrationDto = buildAccommodationReferralOrchestrationDto(
        cas1Referrals = emptyList(),
        cas2HdcReferrals = emptyList(),
        cas2Referrals = emptyList(),
        cas3Referrals = emptyList(),
      )

      every { orchestrationService.fetchAllReferralsAggregated(crn) } returns OrchestrationResultDto(data = orchestrationDto)
      every { dutyToReferQueryService.getDutyToReferHistory(crn) } returns emptyList()

      val result = service.getReferralHistory(crn)

      assertThat(result.data).isEmpty()
    }

    @Test
    fun `should return only DTR when no referrals exist`() {
      val orchestrationDto = buildAccommodationReferralOrchestrationDto(
        cas1Referrals = emptyList(),
        cas2HdcReferrals = emptyList(),
        cas2Referrals = emptyList(),
        cas3Referrals = emptyList(),
      )

      val dutyToReferDto = buildDutyToReferDto(crn = crn)

      every { orchestrationService.fetchAllReferralsAggregated(crn) } returns OrchestrationResultDto(data = orchestrationDto)
      every { dutyToReferQueryService.getDutyToReferHistory(crn) } returns listOf(dutyToReferDto)

      val result = service.getReferralHistory(crn)

      assertThat(result.data).containsExactlyInAnyOrderElementsOf(
        AccommodationReferralTransformer.transformReferrals(
          orchestrationDto,
          listOf(dutyToReferDto),
        ),
      )
    }

    @Test
    fun `should transform all referral types correctly`() {
      val orchestrationDto = buildAccommodationReferralOrchestrationDto()
      val dutyToReferDto = buildDutyToReferDto(crn = crn)

      every { orchestrationService.fetchAllReferralsAggregated(crn) } returns OrchestrationResultDto(data = orchestrationDto)
      every { dutyToReferQueryService.getDutyToReferHistory(crn) } returns listOf(dutyToReferDto)

      val result = service.getReferralHistory(crn)

      assertThat(result.data).hasSize(5)
      assertThat(result.data).containsExactlyInAnyOrderElementsOf(
        AccommodationReferralTransformer.transformReferrals(
          orchestrationDto,
          listOf(dutyToReferDto),
        ),
      )
    }
  }
}
