package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.accommodation

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildAccommodationOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildUpstreamFailure
import java.time.LocalDate
import java.util.UUID

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
            buildAddress(
              cprAddressId = null,
              noFixedAbode = false,
              postcode = "GL53 8GH",
              thoroughfareName = "Another Road",
              postTown = "Cheltenham",
              status = CanonicalAddressStatus(
                code = AddressStatusCode.PR.name,
                description = AddressStatusCode.PR.description,
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
              postcode = null,
              thoroughfareName = null,
              postTown = null,
              startDate = LocalDate.of(2024, 10, 17),
              endDate = LocalDate.of(2025, 10, 17),
              status = CanonicalAddressStatus(
                code = AddressStatusCode.P.name,
                description = AddressStatusCode.P.description,
              ),
              usage = CanonicalAddressUsage(
                usageCode = CanonicalAddressUsageCode(
                  code = AddressUsageCode.A08A.name,
                  description = AddressUsageCode.A08A.description,
                ),
                isActive = true,
              ),
            ),
          ),
        ),
      ),
      upstreamFailures = emptyList(),
    )

    val result = accommodationQueryService.getAccommodationHistory(crn)

    assertThat(result.data.size).isEqualTo(2)
    assertThat(result.data[0].address.postcode).isEqualTo("SW1A 1AA")
    assertThat(result.data[0].status!!.code).isEqualTo(AddressStatusCode.M.name)
    assertThat(result.data[1].address.postcode).isNull()
    assertThat(result.data[1].status!!.code).isEqualTo(AddressStatusCode.P.name)
    assertThat(result.upstreamFailures.size).isEqualTo(0)
  }

  @Test
  fun `getAccommodationHistory should return empty list when cpr addresses call fails`() {
    every { accommodationOrchestrationService.getCorePersonRecordByCrn(crn) } returns OrchestrationResultDto(
      data = buildAccommodationOrchestrationDto(
        cpr = null,
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
  fun `getCurrentAccommodation should orchestrate calls and get the current accommodation`() {
    every { accommodationOrchestrationService.getCorePersonRecordByCrn(crn) } returns OrchestrationResultDto(
      data = buildAccommodationOrchestrationDto(
        cpr = buildCorePersonRecord(
          addresses = listOf(
            buildAddress(
              cprAddressId = null,
              noFixedAbode = false,
              postcode = "SW1A 1AA",
              thoroughfareName = "Some Street",
              postTown = "London",
              status = CanonicalAddressStatus(
                code = AddressStatusCode.M.name,
                description = AddressStatusCode.M.description,
              ),
              usage = CanonicalAddressUsage(
                usageCode = CanonicalAddressUsageCode(
                  code = AddressUsageCode.A01A.name,
                  description = AddressUsageCode.A01A.description,
                ),
                isActive = true,
              ),
            ),
            buildAddress(
              cprAddressId = null,
              noFixedAbode = false,
              postcode = "GL53 8GH",
              thoroughfareName = "",
              postTown = "Cheltenham",
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
          ),
        ),
      ),
      upstreamFailures = emptyList(),
    )

    val result = accommodationQueryService.getCurrentAccommodation(crn)

    assertThat(result.data!!.address.postcode).isEqualTo("SW1A 1AA")
    assertThat(result.data!!.status!!.code).isEqualTo("M")
  }

  @Test
  fun `getCurrentAccommodation should return null data and upstream failure when cpr addresses call fails`() {
    every { accommodationOrchestrationService.getCorePersonRecordByCrn(crn) } returns OrchestrationResultDto(
      data = buildAccommodationOrchestrationDto(
        cpr = null,
      ),
      upstreamFailures = listOf(
        buildUpstreamFailure(
          callKey = ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN,
        ),
      ),
    )

    // when
    val result = accommodationQueryService.getCurrentAccommodation(crn)

    // then
    assertThat(result.data).isNull()
    assertThat(result.upstreamFailures.first().endpoint).isEqualTo(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN)
  }
}
