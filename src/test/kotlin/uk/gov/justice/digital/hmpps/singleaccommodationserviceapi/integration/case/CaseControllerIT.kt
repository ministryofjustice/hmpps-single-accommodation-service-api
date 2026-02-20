package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRosh
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCasesResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedGetCasesWithFilterResponse

class CaseControllerIT : IntegrationTestBase() {

  private val crn = "FAKECRN1"
  private val crn2 = "FAKECRN2"

  @BeforeEach
  fun setup() {
    val corePersonRecord = buildCorePersonRecord(identifiers = buildIdentifiers(crns = listOf(crn)))
    val corePersonRecord2 =
      buildCorePersonRecord(identifiers = buildIdentifiers(crns = listOf(crn)), firstName = "Zack", lastName = "Smith")
    val caseSummaries = CaseSummaries(listOf(buildCaseSummary(crn = crn), buildCaseSummary(crn = crn2)))
    val roshVeryHigh = buildRoshDetails(rosh = buildRosh(riskChildrenCommunity = RiskLevel.VERY_HIGH))
    val roshMedium = buildRoshDetails(rosh = buildRosh(riskChildrenCommunity = RiskLevel.MEDIUM))
    val tier = buildTier()

    hmppsAuth.stubGrantToken()

    // bulk call
    probationIntegrationDeliusMockServer.stubPostCaseSummariesOKResponse(response = caseSummaries)

    corePersonRecordMockServer.stubGetCorePersonRecordOKResponse(crn = crn, response = corePersonRecord)
    corePersonRecordMockServer.stubGetCorePersonRecordOKResponse(crn = crn2, response = corePersonRecord2)

    probationIntegrationOasysMockServer.stubGetRoshOKResponse(crn = crn, roshVeryHigh)
    probationIntegrationOasysMockServer.stubGetRoshOKResponse(crn = crn2, roshMedium)

    tierMockServer.stubGetCorePersonRecordOKResponse(crn = crn, tier)
    tierMockServer.stubGetCorePersonRecordOKResponse(crn = crn2, tier)
  }

  @Test
  fun `should get cases`() {
    restTestClient.get().uri { builder ->
      builder.path("/cases")
        .queryParam("crns", crn, crn2)
        .build()
    }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetCasesResponse())
      }
  }

  @Test
  fun `should get cases with correct filters`() {
    restTestClient.get().uri { builder ->
      builder.path("/cases")
        .queryParam("crns", crn, crn2)
        .queryParam("riskLevel", RiskLevel.MEDIUM.name)
        .build()
    }
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetCasesWithFilterResponse())
      }
  }
}
