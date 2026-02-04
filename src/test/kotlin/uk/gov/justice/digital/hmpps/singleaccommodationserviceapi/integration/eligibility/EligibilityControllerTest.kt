package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPrisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response.expectedGetEligibilityResponse
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.util.UUID

class EligibilityControllerTest : IntegrationTestBase() {
  private val crn = "FAKECRN1"
  private val prisonerNumber = "1234567"
  private val cas1ApplicationId = UUID.randomUUID()

  @BeforeEach
  fun setup() {
    val corePersonRecord = buildCorePersonRecord(
      identifiers = buildIdentifiers(
        crns = listOf(crn),
        prisonNumbers = listOf(prisonerNumber),
      ),
    )
    val tier = buildTier(TierScore.A1)
    val cas1Application = buildCas1Application(id = cas1ApplicationId)
    val prisoner = buildPrisoner(prisonerNumber = prisonerNumber)

    hmppsAuth.stubGrantToken()

    corePersonRecordMockServer.stubGetCorePersonRecordOKResponse(crn = crn, response = corePersonRecord)

    approvedPremisesMockServer.stubGetSuitableApplicationOKResponse(crn = crn, response = cas1Application)

    tierMockServer.stubGetCorePersonRecordOKResponse(crn = crn, tier)

    prisonerSearchMockServer.stubGetPrisonerOKResponse(prisonerNumber = prisonerNumber, prisoner)
  }

  @WithMockAuthUser(roles = ["ROLE_PROBATION"])
  @Test
  fun `should get eligibility for crn`() {
    val result = mockMvc.perform(
      get("/cases/$crn/eligibility"),
    )
      .andExpect(status().isOk)
      .andReturn()
      .response
      .contentAsString

    assertThatJson(result).matchesExpectedJson(expectedGetEligibilityResponse(crn, cas1ApplicationId))
  }
}
