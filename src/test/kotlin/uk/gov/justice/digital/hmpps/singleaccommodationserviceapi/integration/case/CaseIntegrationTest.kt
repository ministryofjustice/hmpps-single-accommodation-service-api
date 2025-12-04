package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildCaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildRoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response.expectedCaseDtoResponseMultipleJson
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser

class CaseIntegrationTest : IntegrationTestBase() {

  @WithMockAuthUser(roles = ["ROLE_PROBATION"])
  @Test
  fun `returns correctly populated CaseDtos`() {
    val crn = "XX12345X"
    val crn2 = "XY12345Z"
    val corePersonRecord = buildCorePersonRecord(crn = crn)
    val corePersonRecord2 = buildCorePersonRecord(crn = crn, firstName = "Zack", lastName = "Smith")
    val caseSummaries = CaseSummaries(listOf(buildCaseSummary(crn = crn), buildCaseSummary(crn = crn2)))
    val roshDetails = buildRoshDetails()
    val tier = buildTier()

    hmppsAuth.stubGrantToken()

    // bulk call
    probationIntegrationDeliusMockServer.stubPostCaseSummariesOKResponse(
      body = listOf(crn, crn2),
      response = caseSummaries,
    )

    corePersonRecordMockServer.stubGetCorePersonRecordOKResponse(crn = crn, response = corePersonRecord)
    corePersonRecordMockServer.stubGetCorePersonRecordOKResponse(crn = crn2, response = corePersonRecord2)

    probationIntegrationOasysMockServer.stubGetRoshOKResponse(crn = crn, roshDetails)
    probationIntegrationOasysMockServer.stubGetRoshOKResponse(crn = crn2, roshDetails)

    tierMockServer.stubGetCorePersonRecordOKResponse(crn = crn, tier)
    tierMockServer.stubGetCorePersonRecordOKResponse(crn = crn2, tier)

    // TODO this call will need to change when we implement a
    val result = mockMvc.perform(get("/cases"))
      .andExpect(status().isOk)
      .andReturn()
      .response
      .contentAsString

    assertThatJson(result).matchesExpectedJson(expectedCaseDtoResponseMultipleJson)
  }
}
