package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.accommodation

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.AddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecordAddresses
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildAccommodationOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildUpstreamFailure

@ExtendWith(MockKExtension::class)
class AccommodationQueryServiceTest {
  @MockK
  lateinit var accommodationOrchestrationService: AccommodationOrchestrationService

  @InjectMockKs
  lateinit var accommodationQueryService: AccommodationQueryService
  private val crn = "X12345"

  @BeforeEach
  fun setup() {
    accommodationQueryService = AccommodationQueryService(accommodationOrchestrationService)
  }

  @Test
  fun `getAccommodationHistory should orchestrate calls and map addresses`() {
    every { accommodationOrchestrationService.getCorePersonRecordByCrn(crn) } returns OrchestrationResultDto(
      data = buildAccommodationOrchestrationDto(
        cpr = buildCorePersonRecord(
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
            buildAddress(
              cprAddressId = null,
              noFixedAbode = false,
              postcode = "GL53 8GH",
              thoroughfareName = "Another Road",
              postTown = "Cheltenham",
              addressStatus = null,
              addressUsage = null,
            ),
          ),
        ),
        cprAddresses = null,
      ),
      upstreamFailures = emptyList(),
    )

    val result = accommodationQueryService.getAccommodationHistory(crn)

    assertThat(result.data.size).isEqualTo(2)
    assertThat(result.data.first().address.postcode).isEqualTo("SW1A 1AA")
    assertThat(result.data[1].address.postcode).isEqualTo("GL53 8GH")
    assertThat(result.upstreamFailures.size).isEqualTo(0)
  }

  @Test
  fun `getAccommodationHistory should return empty list when cpr addresses call fails`() {
    every { accommodationOrchestrationService.getCorePersonRecordByCrn(crn) } returns OrchestrationResultDto(
      data = buildAccommodationOrchestrationDto(
        cpr = null,
        cprAddresses = null,
      ),
      upstreamFailures = listOf(
        buildUpstreamFailure(
          callKey = ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN,
        ),
      ),
    )

    // when
    val result = accommodationQueryService.getAccommodationHistory(crn)

    // then
    assertThat(result.data.size).isEqualTo(0)
    assertThat(result.upstreamFailures.first().endpoint).isEqualTo(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN)
  }

  @Test
  fun `getAccommodationHistoryV2 should orchestrate calls and map addresses`() {
    every { accommodationOrchestrationService.getCorePersonRecordAddressesByCrn(crn) } returns OrchestrationResultDto(
      data = buildAccommodationOrchestrationDto(
        cpr = null,
        cprAddresses = buildCorePersonRecordAddresses(
          addresses = listOf(
            buildAddress(
              cprAddressId = null,
              noFixedAbode = false,
              postcode = "SW1A 1AA",
              thoroughfareName = "Some Street",
              postTown = "London",
              addressStatus = AddressStatus.M,
              addressUsage = buildAddressUsage(
                addressUsageCode = AddressUsageCode.A01A,
                addressUsageDescription = "Householder (Owner - freehold or leasehold)",
              ),
            ),
            buildAddress(
              cprAddressId = null,
              noFixedAbode = false,
              postcode = "GL53 8GH",
              thoroughfareName = "",
              postTown = "Cheltenham",
              addressStatus = AddressStatus.P,
              addressUsage = buildAddressUsage(
                addressUsageCode = AddressUsageCode.A07A,
                addressUsageDescription = "Friends/Family (transient)",
              ),
            ),
          ),
        ),
      ),
      upstreamFailures = emptyList(),
    )

    val result = accommodationQueryService.getAccommodationHistoryV2(crn)

    assertThat(result.data.size).isEqualTo(2)
    assertThat(result.data.first().address.postcode).isEqualTo("SW1A 1AA")
    assertThat(result.data.first().status!!.code).isEqualTo(AccommodationStatusCode.M)
    assertThat(result.data[1].address.postcode).isEqualTo("GL53 8GH")
    assertThat(result.data[1].status!!.code).isEqualTo(AccommodationStatusCode.P)
    assertThat(result.upstreamFailures.size).isEqualTo(0)
  }

  @Test
  fun `getAccommodationHistoryV2 should return empty list when cpr addresses call fails`() {
    every { accommodationOrchestrationService.getCorePersonRecordAddressesByCrn(crn) } returns OrchestrationResultDto(
      data = buildAccommodationOrchestrationDto(
        cpr = null,
        cprAddresses = null,
      ),
      upstreamFailures = listOf(
        buildUpstreamFailure(
          callKey = ApiCallKeys.GET_CORE_PERSON_RECORD_ADDRESSES_BY_CRN,
        ),
      ),
    )

    // when
    val result = accommodationQueryService.getAccommodationHistoryV2(crn)

    // then
    assertThat(result.data.size).isEqualTo(0)
    assertThat(result.upstreamFailures.first().endpoint).isEqualTo(ApiCallKeys.GET_CORE_PERSON_RECORD_ADDRESSES_BY_CRN)
  }

  @Test
  fun `getCurrentAccommodation should orchestrate calls and get the current accommodation`() {
    every { accommodationOrchestrationService.getCorePersonRecordAddressesByCrn(crn) } returns OrchestrationResultDto(
      data = buildAccommodationOrchestrationDto(
        cpr = null,
        cprAddresses = buildCorePersonRecordAddresses(
          addresses = listOf(
            buildAddress(
              cprAddressId = null,
              noFixedAbode = false,
              postcode = "SW1A 1AA",
              thoroughfareName = "Some Street",
              postTown = "London",
              addressStatus = AddressStatus.M,
              addressUsage = buildAddressUsage(
                addressUsageCode = AddressUsageCode.A01A,
                addressUsageDescription = "Householder (Owner - freehold or leasehold)",
              ),
            ),
            buildAddress(
              cprAddressId = null,
              noFixedAbode = false,
              postcode = "GL53 8GH",
              thoroughfareName = "",
              postTown = "Cheltenham",
              addressStatus = AddressStatus.P,
              addressUsage = buildAddressUsage(
                addressUsageCode = AddressUsageCode.A07A,
                addressUsageDescription = "Friends/Family (transient)",
              ),
            ),
          ),
        ),
      ),
      upstreamFailures = emptyList(),
    )

    val result = accommodationQueryService.getCurrentAccommodation(crn)

    assertThat(result.data!!.address.postcode).isEqualTo("SW1A 1AA")
    assertThat(result.data!!.status!!.code).isEqualTo(AccommodationStatusCode.M)
  }

  @Test
  fun `getCurrentAccommodation should return null data and upstream failure when cpr addresses call fails`() {
    every { accommodationOrchestrationService.getCorePersonRecordAddressesByCrn(crn) } returns OrchestrationResultDto(
      data = buildAccommodationOrchestrationDto(
        cpr = null,
        cprAddresses = null,
      ),
      upstreamFailures = listOf(
        buildUpstreamFailure(
          callKey = ApiCallKeys.GET_CORE_PERSON_RECORD_ADDRESSES_BY_CRN,
        ),
      ),
    )

    // when
    val result = accommodationQueryService.getCurrentAccommodation(crn)

    // then
    assertThat(result.data).isNull()
    assertThat(result.upstreamFailures.first().endpoint).isEqualTo(ApiCallKeys.GET_CORE_PERSON_RECORD_ADDRESSES_BY_CRN)
  }
}
