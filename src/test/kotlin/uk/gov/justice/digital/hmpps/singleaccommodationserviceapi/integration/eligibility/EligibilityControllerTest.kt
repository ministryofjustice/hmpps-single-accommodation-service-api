package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPrisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response.expectedGetEligibilityResponse

class EligibilityControllerTest : IntegrationTestBase() {
  private val crn = "X371199"
  private val prisonerNumber = "1234567"

  @BeforeEach
  fun setup() {
    val corePersonRecord = buildCorePersonRecord(
      identifiers = buildIdentifiers(
        crns = listOf(crn),
        prisonNumbers = listOf(prisonerNumber),
      ),
    )
    val tier = buildTier()
    val cas1Application = buildCas1Application()
    val prisoner = buildPrisoner(prisonerNumber = prisonerNumber)

    hmppsAuth.stubGrantToken()

    corePersonRecordMockServer.stubGetCorePersonRecordOKResponse(crn = crn, response = corePersonRecord)

    approvedPremisesMockServer.stubGetSuitableApplicationOKResponse(crn = crn, response = cas1Application)

    tierMockServer.stubGetCorePersonRecordOKResponse(crn = crn, tier)

    prisonerSearchMockServer.stubGetPrisonerOKResponse(prisonerNumber = prisonerNumber, prisoner)
  }

  @Test
  fun `should get eligibility for crn`() {
    restTestClient.get().uri("/cases/{crn}/eligibility", crn)
      .withJwt()
      .exchange().expectStatus().isOk
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetEligibilityResponse(crn))
      }
  }
}
