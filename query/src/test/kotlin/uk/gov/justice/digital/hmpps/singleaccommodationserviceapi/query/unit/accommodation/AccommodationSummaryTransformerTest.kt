package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.accommodation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.AddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationSummaryTransformer
import java.time.LocalDate
import java.util.UUID

class AccommodationSummaryTransformerTest {

  @Test
  fun `should map all fields when address has status and usage`() {
    val address = buildAddress(
      cprAddressId = UUID.randomUUID(),
      noFixedAbode = false,
      startDate = LocalDate.of(2023, 1, 1),
      endDate = LocalDate.of(2024, 1, 1),
      postcode = "NW1 6XE",
      subBuildingName = "Flat 4B",
      buildingName = "Camden Heights",
      buildingNumber = "12",
      thoroughfareName = "Camden High Street",
      dependentLocality = "Camden Town",
      postTown = "London",
      county = "Greater London",
      country = "United Kingdom",
      countryCode = "GB",
      addressStatus = AddressStatus.M,
      addressUsage = buildAddressUsage(
        addressUsageCode = AddressUsageCode.A01A,
        addressUsageDescription = "Owner occupier",
      ),
      uprn = "100012345678",
    )

    val result = AccommodationSummaryTransformer.toAccommodationSummary(
      crn = "X92123",
      address = address,
    )

    assertThat(result.crn).isEqualTo("X92123")
    assertThat(result.startDate).isEqualTo(address.startDate)
    assertThat(result.endDate).isEqualTo(address.endDate)
    assertThat(result.status!!.code).isEqualTo(AccommodationStatusCode.M)
    assertThat(result.status!!.description).isEqualTo(AccommodationStatusCode.M.description)
    assertThat(result.type!!.code).isEqualTo(AccommodationTypeCode.A01A)
    assertThat(result.type!!.description).isEqualTo(AccommodationTypeCode.A01A.description)
    assertThat(result.address.postcode).isEqualTo("NW1 6XE")
    assertThat(result.address.subBuildingName).isEqualTo("Flat 4B")
    assertThat(result.address.buildingName).isEqualTo("Camden Heights")
    assertThat(result.address.buildingNumber).isEqualTo("12")
    assertThat(result.address.thoroughfareName).isEqualTo("Camden High Street")
    assertThat(result.address.dependentLocality).isEqualTo("Camden Town")
    assertThat(result.address.postTown).isEqualTo("London")
    assertThat(result.address.county).isEqualTo("Greater London")
    assertThat(result.address.country).isEqualTo("GB")
    assertThat(result.address.uprn).isEqualTo("100012345678")
  }

  @Test
  fun `should return null status and type when addressStatus and addressUsage are null`() {
    val address = buildAddress(
      addressStatus = null,
      addressUsage = null,
    )
    val result = AccommodationSummaryTransformer.toAccommodationSummary(
      crn = "X92123",
      address = address,
    )
    assertThat(result.status).isNull()
    assertThat(result.type).isNull()
  }

  @Test
  fun `should handle null address fields`() {
    val address = buildAddress(
      postcode = null,
      subBuildingName = null,
      buildingName = null,
      buildingNumber = null,
      thoroughfareName = null,
      dependentLocality = null,
      postTown = null,
      county = null,
      countryCode = null,
      uprn = null,
    )

    val result = AccommodationSummaryTransformer.toAccommodationSummary(
      crn = "X92123",
      address = address,
    )

    assertThat(result.address.postcode).isNull()
    assertThat(result.address.subBuildingName).isNull()
    assertThat(result.address.buildingName).isNull()
    assertThat(result.address.buildingNumber).isNull()
    assertThat(result.address.thoroughfareName).isNull()
    assertThat(result.address.dependentLocality).isNull()
    assertThat(result.address.postTown).isNull()
    assertThat(result.address.county).isNull()
    assertThat(result.address.country).isNull()
    assertThat(result.address.uprn).isNull()
  }
}
