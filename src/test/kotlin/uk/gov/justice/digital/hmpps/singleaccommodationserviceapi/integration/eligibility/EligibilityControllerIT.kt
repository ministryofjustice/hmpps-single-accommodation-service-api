package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.AddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecordAddresses
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response.expectedGetEligibilityResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ApprovedPremisesStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs
import java.time.LocalDate
import java.util.UUID

class EligibilityControllerIT : IntegrationTestBase() {
  private val crn = "FAKECRN1"
  private val cas1ApplicationId = UUID.fromString("e6b202ce-c214-4b87-98f5-111111111111")
  private val cas3ApplicationId = UUID.fromString("e6b202ce-c214-4b87-98f6-111111111111")

  @BeforeEach
  fun setup() {
    val corePersonRecord = buildCorePersonRecord(
      identifiers = buildIdentifiers(
        crns = listOf(crn),
      ),
    )
    val corePersonRecordAddresses = buildCorePersonRecordAddresses(
      crn = crn,
      addresses = listOf(
        buildAddress(
          addressStatus = AddressStatus.M,
          endDate = LocalDate.now().plusDays(1),
        ),
      ),
    )
    val tier = buildTier(TierScore.A1)
    val cas1Application = buildCas1Application(id = cas1ApplicationId)
    val cas3Application = buildCas3Application(id = cas3ApplicationId)

    HmppsAuthStubs.stubGrantToken()
    createTestDataSetupUserAndDeliusUser()

    CorePersonRecordStubs.getCorePersonRecordOKResponse(crn = crn, response = corePersonRecord)
    CorePersonRecordStubs.getCorePersonRecordAddressesOKResponse(crn = crn, response = corePersonRecordAddresses)
    ApprovedPremisesStubs.getCas1SuitableApplicationOKResponse(crn = crn, response = cas1Application)
    ApprovedPremisesStubs.getCas3SuitableApplicationOKResponse(crn = crn, response = cas3Application)

    TierStubs.getTierOKResponse(crn = crn, tier)
  }

  @Test
  fun `should get eligibility for crn`() {
    restTestClient.get().uri("/cases/{crn}/eligibility", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetEligibilityResponse(crn, cas1ApplicationId, cas3ApplicationId))
      }
  }
}
