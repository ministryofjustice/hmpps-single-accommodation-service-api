package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.accommodation

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.AddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecordAddresses
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.accommodation.json.expectedGetCurrentAccommodationResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.accommodation.json.expectedGetCurrentAccommodationWithUpstreamFailureResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import java.time.LocalDate
import java.util.UUID

class AccommodationControllerIT : IntegrationTestBase() {
  private val crn = "FAKECRN"

  @BeforeEach
  fun setup() {
    createTestDataSetupUserAndDeliusUser()
    HmppsAuthStubs.stubGrantToken()
  }

  @Test
  fun `should get current accommodation for crn`() {
    val corePersonRecordAddresses = buildCorePersonRecordAddresses(
      crn = crn,
      addresses = listOf(
        buildAddress(
          cprAddressId = UUID.randomUUID(),
          noFixedAbode = false,
          postcode = "SW1A 1AA",
          thoroughfareName = "Some Street",
          postTown = "London",
          startDate = LocalDate.of(2026, 1, 11),
          endDate = null,
          addressStatus = AddressStatus.M,
          addressUsage = buildAddressUsage(
            addressUsageCode = AddressUsageCode.A07B,
            addressUsageDescription = "Friends/Family (settled)",
          ),
        ),
        buildAddress(
          cprAddressId = UUID.randomUUID(),
          noFixedAbode = false,
          postcode = "SW1A 1AA",
          thoroughfareName = "Some Street",
          postTown = "London",
          startDate = LocalDate.of(2025, 10, 17),
          endDate = LocalDate.of(2026, 1, 10),
          addressStatus = AddressStatus.P,
          addressUsage = buildAddressUsage(
            addressUsageCode = AddressUsageCode.A07A,
            addressUsageDescription = "Friends/Family (transient)",
          ),
        ),
      ),
    )
    CorePersonRecordStubs.getCorePersonRecordAddressesOKResponse(
      crn = crn,
      response = corePersonRecordAddresses,
    )
    restTestClient.get().uri("/cases/{crn}/accommodations/current", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetCurrentAccommodationResponse())
      }
  }

  @Test
  fun `get current accommodation should return partial success when CPR Addresses call returns server error`() {
    CorePersonRecordStubs.getCorePersonRecordAddressesErrorResponse(crn)
    restTestClient.get().uri("/cases/{crn}/accommodations/current", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetCurrentAccommodationWithUpstreamFailureResponse())
      }
  }
}
