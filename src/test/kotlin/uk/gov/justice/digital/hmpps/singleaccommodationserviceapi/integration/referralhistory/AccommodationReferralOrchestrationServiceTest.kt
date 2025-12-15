package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.referralhistory

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildCas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildCas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildCas2v2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildCas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.time.Instant
import java.util.UUID

class AccommodationReferralOrchestrationServiceTest : IntegrationTestBase() {

  @BeforeEach
  fun setup() {
    hmppsAuth.stubGrantToken()
  }

  @WithMockAuthUser(roles = ["ROLE_PROBATION"])
  @Test
  fun `fetchAllReferralsAggregated aggregates results and sorts them by date descending`() {
    val crn = "X12345"

    val cas1Response = buildCas1ReferralHistory(id = UUID.randomUUID(), createdAt = Instant.parse("2025-03-01T00:00:00Z"))
    val cas2Response = buildCas2ReferralHistory(id = UUID.randomUUID(), createdAt = Instant.parse("2025-01-01T00:00:00Z"))
    val cas2v2Response = buildCas2v2ReferralHistory(id = UUID.randomUUID(), createdAt = Instant.parse("2025-04-01T00:00:00Z"))
    val cas3Response = buildCas3ReferralHistory(id = UUID.randomUUID(), createdAt = Instant.parse("2025-02-01T00:00:00Z"))

    approvedPremisesMockServer.stubGetCas1ReferralOKResponse(crn, cas1Response)
    approvedPremisesMockServer.stubGetCas2ReferralOKResponse(crn, cas2Response)
    approvedPremisesMockServer.stubGetCas2v2ReferralOKResponse(crn, cas2v2Response)
    approvedPremisesMockServer.stubGetCas3ReferralOKResponse(crn, cas3Response)

    val responseContent = mockMvc.perform(
      get("/application-histories/$crn"),
    )
      .andExpect(status().isOk)
      .andReturn()
      .response
      .contentAsString

    assertThatJson(responseContent).matchesExpectedJson(
      expectedGetReferralHistory(
        id1 = cas1Response.first().id,
        id2 = cas2Response.first().id,
        id3 = cas2v2Response.first().id,
        id4 = cas3Response.first().id,
      ),
    )
  }
}
