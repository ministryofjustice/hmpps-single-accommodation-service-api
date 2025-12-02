package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import com.fasterxml.jackson.core.type.TypeReference
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
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
  fun `returns a populated CaseDto`() {
    val crn = "XX12345X"
    val corePersonRecord = buildCorePersonRecord(crn = crn)
    val caseSummaries = CaseSummaries(listOf(buildCaseSummary(crn = crn)))
    val roshDetails = buildRoshDetails()
    val tier = buildTier()

    hmppsAuth.stubGrantToken()
    corePersonRecordMockServer.stubGetCorePersonRecordOKResponse(crn, corePersonRecord)
    probationIntegrationDeliusMockServer.stubPostCaseSummariesOKResponse(body = listOf(crn), response = caseSummaries)
    probationIntegrationOasysMockServer.stubGetRoshOKResponse(crn = crn, roshDetails)
    tierMockServer.stubGetCorePersonRecordOKResponse(crn = crn, tier)

    val result = mockMvc.perform(get("/cases?crns=XX12345X"))
      .andExpect(status().isOk)
      .andReturn().response.contentAsString

    val cases = objectMapper.readValue(result, object : TypeReference<List<Case>>() {})

    assertThat(result).isNotBlank
  }
}
