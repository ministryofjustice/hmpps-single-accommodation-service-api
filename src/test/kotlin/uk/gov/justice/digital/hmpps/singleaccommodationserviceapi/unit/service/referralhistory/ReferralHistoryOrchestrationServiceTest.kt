package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.service.referralhistory

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.AggregatorResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.CasReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.CasType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.models.Referral
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CAS1_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CAS2V2_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CAS2_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CAS3_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.service.referralhistory.ReferralHistoryOrchestrationService
import java.time.Instant
import java.util.UUID

@ExtendWith(MockKExtension::class)
class ReferralHistoryOrchestrationServiceTest {

  @MockK
  lateinit var aggregatorService: AggregatorService

  @MockK
  lateinit var approvedPremisesCachingService: ApprovedPremisesCachingService

  private lateinit var service: ReferralHistoryOrchestrationService

  @BeforeEach
  fun setup() {
    service = ReferralHistoryOrchestrationService(aggregatorService, approvedPremisesCachingService)
  }

  @Test
  fun `fetchAllReferralsAggregated aggregates results and sorts them by date descending`() {
    val crn = "X12345"

    val t1 = Instant.parse("2025-01-01T00:00:00Z")
    val t2 = Instant.parse("2025-02-01T00:00:00Z")
    val t3 = Instant.parse("2025-03-01T00:00:00Z")
    val t4 = Instant.parse("2025-04-01T00:00:00Z")

    val cas1 = listOf(Referral(UUID.randomUUID(), CasType.CAS1, CasReferralStatus.PENDING, t3))
    val cas2 = listOf(Referral(UUID.randomUUID(), CasType.CAS2, CasReferralStatus.ACCEPTED, t1))
    val cas2v2 = listOf(Referral(UUID.randomUUID(), CasType.CAS2v2, CasReferralStatus.REJECTED, t4))
    val cas3 = listOf(Referral(UUID.randomUUID(), CasType.CAS3, CasReferralStatus.PENDING, t2))

    every {
      aggregatorService.orchestrateAsyncCalls(
        standardCallsNoIteration = any(),
        callsPerIdentifier = null
      )
    } returns AggregatorResult(
      standardCallsNoIterationResults = mapOf(
        GET_CAS1_REFERRAL to cas1,
        GET_CAS2_REFERRAL to cas2,
        GET_CAS2V2_REFERRAL to cas2v2,
        GET_CAS3_REFERRAL to cas3,
      ),
      callsPerIdentifierResults = null,
    )

    val result = service.fetchAllReferralsAggregated(crn)

    assertThat(result.map { it.date }).containsExactly(t4, t3, t2, t1)
    assertThat(result).hasSize(4)

    verify(exactly = 1) {
      aggregatorService.orchestrateAsyncCalls(standardCallsNoIteration = any(), callsPerIdentifier = null)
    }
  }
}
