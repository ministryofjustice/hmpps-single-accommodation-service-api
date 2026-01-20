package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain.AccommodationType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCrs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToRefer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPrisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response.expectedGetEligibilityResponse
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.time.LocalDate
import java.util.UUID

class EligibilityControllerTest : IntegrationTestBase() {
  private val crn = "FAKECRN1"
  private val prisonerNumber = "1234567"
  private val cas1ApplicationId = UUID.randomUUID()
  private val cas3ApplicationId = UUID.randomUUID()

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
    val cas3Application = buildCas3Application(id = cas3ApplicationId)
    val releaseDate = LocalDate.now().plusDays(30)
    val prisoner = buildPrisoner(prisonerNumber = prisonerNumber, releaseDate = releaseDate)
    val currentAccommodation = buildAccommodation(type = AccommodationType.PRISON)
    val dutyToRefer = buildDutyToRefer(status = "submitted")
    val crs = buildCrs(status = "submitted")

    hmppsAuth.stubGrantToken()

    corePersonRecordMockServer.stubGetCorePersonRecordOKResponse(crn = crn, response = corePersonRecord)

    approvedPremisesMockServer.stubGetSuitableApplicationOKResponse(crn = crn, response = cas1Application)
    approvedPremisesMockServer.stubGetSuitableCas3ApplicationOKResponse(crn = crn, response = cas3Application)

    tierMockServer.stubGetCorePersonRecordOKResponse(crn = crn, tier)

    prisonerSearchMockServer.stubGetPrisonerOKResponse(prisonerNumber = prisonerNumber, prisoner)

    accommodationDataDomainMockServer.stubGetDutyToReferOKResponse(crn = crn, response = dutyToRefer)
    accommodationDataDomainMockServer.stubGetCrsOKResponse(crn = crn, response = crs)
    accommodationDataDomainMockServer.stubGetCurrentAccommodationOKResponse(response = currentAccommodation)
    accommodationDataDomainMockServer.stubGetProposedAccommodationsOKResponse(response = emptyList())
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

    assertThatJson(result).matchesExpectedJson(expectedGetEligibilityResponse(crn, cas1ApplicationId, cas3ApplicationId))
  }
}
