package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.accommodation

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
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
    val corePersonRecord = buildCorePersonRecord(
      identifiers = buildIdentifiers(crns = listOf(crn)),
      addresses = listOf(
        buildAddress(
          cprAddressId = UUID.randomUUID(),
          noFixedAbode = false,
          postcode = "W5 2AB",
          thoroughfareName = "Another Street",
          postTown = "London",
          startDate = LocalDate.of(2025, 10, 17),
          endDate = LocalDate.of(2026, 1, 10),
          status = CanonicalAddressStatus(
            code = AddressStatusCode.P.name,
            description = AddressStatusCode.P.description,
          ),
          usage = CanonicalAddressUsage(
            usageCode = CanonicalAddressUsageCode(
              code = AddressUsageCode.A07A.name,
              description = AddressUsageCode.A07A.description,
            ),
            isActive = true,
          ),
        ),
        buildAddress(
          cprAddressId = UUID.randomUUID(),
          noFixedAbode = false,
          postcode = "SW1A 1AA",
          thoroughfareName = "Some Street",
          postTown = "London",
          startDate = LocalDate.of(2026, 1, 11),
          endDate = null,
          status = CanonicalAddressStatus(
            code = AddressStatusCode.M.name,
            description = AddressStatusCode.M.description,
          ),
          usage = CanonicalAddressUsage(
            usageCode = CanonicalAddressUsageCode(
              code = AddressUsageCode.A07B.name,
              description = AddressUsageCode.A07B.description,
            ),
            isActive = true,
          ),
        ),
      ),
    )
    CorePersonRecordStubs.getCorePersonRecordOKResponse(
      crn = crn,
      response = corePersonRecord,
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
    CorePersonRecordStubs.getCorePersonRecordServerErrorResponse(crn)
    restTestClient.get().uri("/cases/{crn}/accommodations/current", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(expectedGetCurrentAccommodationWithUpstreamFailureResponse())
      }
  }
}
