package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.accommodationhistory

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.AddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecordAddresses
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.accommodationhistory.json.expectedGetAccommodationHistoryResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.accommodationhistory.json.expectedGetAccommodationHistoryV2Response
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.accommodationhistory.json.expectedGetAccommodationHistoryV2WithUpstreamFailureResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.accommodationhistory.json.expectedGetAccommodationHistoryWithUpstreamFailureResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import java.util.UUID

class AccommodationHistoryControllerIT : IntegrationTestBase() {
  private val crn = "FAKECRN"

  @BeforeEach
  fun setup() {
    createTestDataSetupUserAndDeliusUser()
    HmppsAuthStubs.stubGrantToken()
  }

  @Test
  fun `should get accommodation history for crn`() {
    val corePersonRecord = buildCorePersonRecord(
      identifiers = buildIdentifiers(crns = listOf(crn)),
      addresses = listOf(
        buildAddress(
          cprAddressId = null,
          noFixedAbode = false,
          postcode = "SW1A 1AA",
          thoroughfareName = "Some Street",
          postTown = "London",
          addressStatus = null,
          addressUsage = null,
        ),
      ),
    )
    CorePersonRecordStubs.getCorePersonRecordOKResponse(
      crn = crn,
      response = corePersonRecord,
    )
    restTestClient.get().uri("/cases/{crn}/accommodation-history", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetAccommodationHistoryResponse())
      }
  }

  @Test
  fun `get accommodation history for crn should return partial success when CPR call returns server error`() {
    CorePersonRecordStubs.getCorePersonRecordServerErrorResponse(crn)
    restTestClient.get().uri("/cases/{crn}/accommodation-history", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetAccommodationHistoryWithUpstreamFailureResponse())
      }
  }

  @Test
  fun `should get accommodation history v2 for crn`() {
    val corePersonRecordAddresses = buildCorePersonRecordAddresses(
      crn = crn,
      addresses = listOf(
        buildAddress(
          cprAddressId = UUID.randomUUID(),
          noFixedAbode = false,
          postcode = "SW1A 1AA",
          thoroughfareName = "Some Street",
          postTown = "London",
          addressStatus = AddressStatus.M,
          addressUsage = buildAddressUsage(
            addressUsageCode = AddressUsageCode.A07B,
            addressUsageDescription = "Friends/Family (settled)",
          ),
        ),
      ),
    )
    CorePersonRecordStubs.getCorePersonRecordAddressesOKResponse(
      crn = crn,
      response = corePersonRecordAddresses,
    )
    restTestClient.get().uri("/v2/cases/{crn}/accommodation-history", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetAccommodationHistoryV2Response())
      }
  }

  @Test
  fun `get accommodation history v2 should return partial success when CPR Addresses call returns server error`() {
    CorePersonRecordStubs.getCorePersonRecordAddressesErrorResponse(crn)
    restTestClient.get().uri("/v2/cases/{crn}/accommodation-history", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetAccommodationHistoryV2WithUpstreamFailureResponse())
      }
  }
}
