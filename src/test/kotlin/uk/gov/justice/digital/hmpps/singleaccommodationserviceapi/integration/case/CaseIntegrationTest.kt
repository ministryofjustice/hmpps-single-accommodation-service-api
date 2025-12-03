package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.factory.buildCaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.factory.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.factory.buildRoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.factory.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.objectMapper
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

    validateByMappingToObject(result, crn, crn2)
    validateByJsonPath(result, crn, crn2)
    validateByExpectedJsonResult(result)

    assertThat("result2").isNotBlank()
  }

  private fun validateByExpectedJsonResult(result: ResultActions) {
    assertThat(result.andReturn().response.contentAsString).isEqualTo(expectedResponse)
  }

  private fun validateByJsonPath(
    result: ResultActions,
    crn: String,
    crn2: String,
  ) {
    result
      .andExpect(jsonPath("$[1].crn").value(crn))
      .andExpect(jsonPath("$[0].crn").value(crn2))
  }
  private fun validateByMappingToObject(result: ResultActions, crn: String, crn2: String) {
    val cases: List<Case> = objectMapper.readValue(result.andReturn().response.contentAsString)

    assertAll(
      { assertThat(cases).hasSize(2) },
      { assertThat(cases[0].crn).isEqualTo(crn2) },
      { assertThat(cases[1].crn).isEqualTo(crn) },
    )
  }
}

private val expectedResponse = """
[{"name":"Zack Middle Smith","dateOfBirth":"2000-12-03","crn":"XY12345Z","prisonNumber":"PRI1","tier":"TODO()","riskLevel":"VERY_HIGH","pncReference":"Some PNC Reference","assignedTo":{"id":1,"name":"TODO(name)"},"currentAccommodation":{"type":"AIRBNB","endDate":"2025-12-13"},"nextAccommodation":{"type":"PRISON","startDate":"2026-03-13"}},{"name":"First Middle Last","dateOfBirth":"2000-12-03","crn":"XX12345X","prisonNumber":"PRI1","tier":"TODO()","riskLevel":"VERY_HIGH","pncReference":"Some PNC Reference","assignedTo":{"id":1,"name":"TODO(name)"},"currentAccommodation":{"type":"AIRBNB","endDate":"2025-12-13"},"nextAccommodation":{"type":"PRISON","startDate":"2026-03-13"}}]
""".trimIndent()
