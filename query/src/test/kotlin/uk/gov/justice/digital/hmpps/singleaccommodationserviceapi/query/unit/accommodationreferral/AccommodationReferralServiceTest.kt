package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.accommodationreferral

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.ApprovedPremisesApplicationStatus.PLACEMENT_ALLOCATED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.ApprovedPremisesApplicationStatus.REJECTED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDeliusUserDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildAccommodationReferralOrchestrationDto
import java.time.LocalDate

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
      val olderDate = LocalDate.parse("2024-01-01")

      val orchestrationDto = buildAccommodationReferralOrchestrationDto(
        cas1Referrals = listOf(
          buildReferralHistory(REJECTED, date = olderDate, referredBy = buildDeliusUserDto()),
        ),
        cas3Referrals = emptyList(),
      )
      val dtrSubmissionDate = LocalDate.of(2025, 5, 1)
      val dutyToReferDto = buildDutyToReferDto(crn = crn, submissionDate = dtrSubmissionDate)

      every { orchestrationService.fetchAllReferralsAggregated(crn) } returns OrchestrationResultDto(data = orchestrationDto)
      every { dutyToReferQueryService.getDutyToReferHistory(crn) } returns listOf(dutyToReferDto)

      val result = service.getReferralHistory(crn)

      assertThat(result.data).hasSize(2)
      assertThat(result.data[0].date).isEqualTo(dtrSubmissionDate)
      assertThat(result.data[1].date).isEqualTo(olderDate)
    }

    @Test
    fun `should return empty list when no referrals exist`() {
      val orchestrationDto = buildAccommodationReferralOrchestrationDto(
        cas1Referrals = emptyList(),
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

      assertThat(result.data).hasSize(3)
      assertThat(result.data).containsExactlyInAnyOrderElementsOf(
        AccommodationReferralTransformer.transformReferrals(
          orchestrationDto,
          listOf(dutyToReferDto),
        ),
      )
    }

    @Test
    fun `should filter out PENDING and ACCEPTED referrals correctly`() {
      // For CAS1 and CAS3: Filter PENDING and ACCEPTED. Keep others (e.g. REJECTED, WITHDRAWN, etc.)
      // For DTR: Filter PENDING only. Keep others (including ACCEPTED).

      val cas1Rejected = buildReferralHistory(
        applicationStatus = REJECTED,
        referredBy = buildDeliusUserDto(),
      )
      val cas1Pending = buildReferralHistory(
        applicationStatus = Cas1ReferralHistory.ApprovedPremisesApplicationStatus.STARTED,
        referredBy = buildDeliusUserDto(),
      )
      val cas1Accepted = buildReferralHistory(
        applicationStatus = PLACEMENT_ALLOCATED,
        referredBy = buildDeliusUserDto(),
      )

      val cas3Rejected = buildReferralHistory(
        applicationStatus = Cas3ReferralHistory.ApplicationStatus.REJECTED,
        assessmentStatus = Cas3ReferralHistory.AssessmentStatus.REJECTED,
        referralRejectionReason = "reason",
      )
      val cas3Pending = buildReferralHistory(
        applicationStatus = Cas3ReferralHistory.ApplicationStatus.SUBMITTED,
        assessmentStatus = Cas3ReferralHistory.AssessmentStatus.READY_TO_PLACE,
      )
      val cas3Accepted = buildReferralHistory(
        applicationStatus = Cas3ReferralHistory.ApplicationStatus.SUBMITTED,
        assessmentStatus = Cas3ReferralHistory.AssessmentStatus.READY_TO_PLACE,
        bookingStatus = Cas3ReferralHistory.Cas3BookingStatus.ARRIVED,
      )

      val dtrPending = buildDutyToReferDto(status = DtrStatus.SUBMITTED)
      val dtrAccepted = buildDutyToReferDto(status = DtrStatus.ACCEPTED)
      val dtrRejected = buildDutyToReferDto(status = DtrStatus.NOT_ACCEPTED)

      val orchestrationDto = buildAccommodationReferralOrchestrationDto(
        cas1Referrals = listOf(cas1Rejected, cas1Pending, cas1Accepted),
        cas3Referrals = listOf(cas3Rejected, cas3Pending, cas3Accepted),
      )

      every { orchestrationService.fetchAllReferralsAggregated(crn) } returns OrchestrationResultDto(data = orchestrationDto)
      every { dutyToReferQueryService.getDutyToReferHistory(crn) } returns listOf(dtrPending, dtrAccepted, dtrRejected)

      val result = service.getReferralHistory(crn)

      // Expected to keep: cas1Rejected, cas3Rejected, dtrAccepted, dtrRejected
      // Expected to filter: cas1Pending, cas1Accepted, cas3Pending, cas3Accepted, dtrPending
      assertThat(result.data).hasSize(4)
      assertThat(result.data.map { it.id }).containsExactlyInAnyOrder(
        cas1Rejected.id,
        cas3Rejected.id,
        dtrAccepted.submission!!.id,
        dtrRejected.submission!!.id,
      )
    }
  }
}
