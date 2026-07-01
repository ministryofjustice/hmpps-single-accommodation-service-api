package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.accommodation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.InOutStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationStatusEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPrisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.toAccommodationDetail
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class AccommodationTransformerTest {
  private val prisonAccommodationTypeCode = "HMP"

  @Nested
  inner class ToAccommodationSummary {

    @Test
    fun `should get accommodation status for next accommodation when current accommodation is HMP`() {
      val result = AccommodationTransformer.getAccommodationStatus(
        currentAccommodation = buildAccommodationSummaryDto(
          type = buildAccommodationTypeDto(
            code = prisonAccommodationTypeCode,
          ),
        ),
      )

      assertThat(result).isEqualTo(
        buildAccommodationStatusDto(
          code = AddressStatusCode.PR1.name,
          description = AddressStatusCode.PR1.description,
        ),
      )
    }

    @Test
    fun `should get accommodation status for next accommodation when current accommodation is not HMP`() {
      val result = AccommodationTransformer.getAccommodationStatus(
        currentAccommodation = buildAccommodationSummaryDto(
          type = buildAccommodationTypeDto(
            code = "NOT_HMP",
          ),
        ),
      )

      assertThat(result).isEqualTo(
        buildAccommodationStatusDto(
          code = AddressStatusCode.PR.name,
          description = AddressStatusCode.PR.description,
        ),
      )
    }

    @Test
    fun `should get accommodation status for next accommodation when current accommodation is null`() {
      val result = AccommodationTransformer.getAccommodationStatus(
        currentAccommodation = null,
      )

      assertThat(result).isEqualTo(
        buildAccommodationStatusDto(
          code = AddressStatusCode.PR.name,
          description = AddressStatusCode.PR.description,
        ),
      )
    }

    @Nested
    inner class ToAccommodationSummaryForCas1Premises {
      @Test
      fun `should map all fields`() {
        val cas1Premises = buildCas1PremisesSummary(
          startDate = LocalDate.of(2023, 1, 1),
          endDate = LocalDate.of(2024, 1, 1),
          postcode = "NW1 6XE",
          addressLine1 = "test1",
          addressLine2 = "test2",
          town = "test3",
        )

        val expected = buildAccommodationSummaryDto(
          crn = "X92123",
          startDate = LocalDate.of(2023, 1, 1),
          endDate = LocalDate.of(2024, 1, 1),
          address = buildAccommodationAddressDetails(
            postcode = "NW1 6XE",
            subBuildingName = null,
            buildingName = null,
            buildingNumber = null,
            thoroughfareName = "test1",
            dependentLocality = "test2",
            postTown = "test3",
            county = null,
            country = null,
            uprn = null,
          ),
          type = buildAccommodationTypeDto(
            code = AddressUsageCode.A02.name,
            description = AddressUsageCode.A02.description,
          ),
          status = buildAccommodationStatusDto(
            code = AddressStatusCode.PR1.name,
            description = AddressStatusCode.PR1.description,
          ),
        )

        val result = AccommodationTransformer.toAccommodationSummary(
          crn = "X92123",
          premises = cas1Premises,
          currentAccommodation = buildAccommodationSummaryDto(
            type = buildAccommodationTypeDto(
              code = prisonAccommodationTypeCode,
            ),
          ),
        )

        assertThat(result).isEqualTo(expected)
      }
    }

    @Nested
    inner class ToAccommodationSummaryForCas3Premises {
      @Test
      fun `should map all fields`() {
        val cas3Premises = buildCas3PremisesSummary(
          startDate = LocalDate.of(2023, 1, 1),
          endDate = LocalDate.of(2024, 1, 1),
          postcode = "NW1 6XE",
          addressLine1 = "test1",
          addressLine2 = "test2",
          town = "test3",
          name = "test4",
        )

        val expected = buildAccommodationSummaryDto(
          crn = "X92123",
          startDate = LocalDate.of(2023, 1, 1),
          endDate = LocalDate.of(2024, 1, 1),
          address = buildAccommodationAddressDetails(
            postcode = "NW1 6XE",
            subBuildingName = null,
            buildingName = "test4",
            buildingNumber = null,
            thoroughfareName = "test1",
            dependentLocality = "test2",
            postTown = "test3",
            county = null,
            country = null,
            uprn = null,
          ),
          type = buildAccommodationTypeDto(
            code = AddressUsageCode.A17.name,
            description = AddressUsageCode.A17.description,
          ),
          status = buildAccommodationStatusDto(
            code = AddressStatusCode.PR1.name,
            description = AddressStatusCode.PR1.description,
          ),
        )

        val result = AccommodationTransformer.toAccommodationSummary(
          crn = "X92123",
          premises = cas3Premises,
          currentAccommodation = buildAccommodationSummaryDto(
            type = buildAccommodationTypeDto(
              code = prisonAccommodationTypeCode,
            ),
          ),
        )

        assertThat(result).isEqualTo(expected)
      }
    }

    @Test
    fun `toAccommodationSummary() should map all fields when address has status and usage`() {
      val address = buildCanonicalAddress(
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
        status = CanonicalAddressStatus(
          code = AddressStatusCode.M.name,
          description = AddressStatusCode.M.description,
        ),
        usages = listOf(
          CanonicalAddressUsage(
            usageCode = CanonicalAddressUsageCode(
              code = AddressUsageCode.A01A.name,
              description = AddressUsageCode.A01A.description,
            ),
            isActive = true,
          ),
        ),
        uprn = "100012345678",
      )

      val result = AccommodationTransformer.toAccommodationSummary(
        crn = "X92123",
        address = address,
      )

      assertThat(result.crn).isEqualTo("X92123")
      assertThat(result.startDate).isEqualTo(address.startDate)
      assertThat(result.endDate).isEqualTo(address.endDate)
      assertThat(result.status!!.code).isEqualTo(AddressStatusCode.M.name)
      assertThat(result.status!!.description).isEqualTo(AddressStatusCode.M.description)
      assertThat(result.type!!.code).isEqualTo(AddressUsageCode.A01A.name)
      assertThat(result.type!!.description).isEqualTo(AddressUsageCode.A01A.description)
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
    fun `toAccommodationSummary() should map all fields when it is a prison`() {
      val crn = "X92123"

      val prisoner = buildPrisoner(
        prisonNumber = "PRI1",
        releaseDate = LocalDate.now(),
        confirmedReleaseDate = LocalDate.now(),
        inOutStatus = InOutStatus.IN,
        prisonId = "SOMETHING",
        prisonName = "SOME PRISON",
        status = "A STATUS",
      )

      val expectedResult = buildAccommodationSummaryDto(
        crn = crn,
        endDate = prisoner.releaseDate,
        address = buildAccommodationAddressDetails(
          subBuildingName = null,
          postcode = null,
          buildingName = prisoner.prisonName,
          buildingNumber = null,
          thoroughfareName = null,
          dependentLocality = null,
          postTown = null,
          county = null,
          country = null,
          uprn = null,
        ),
        status = buildAccommodationStatusDto(
          code = "C",
          description = "Custody",
        ),
        type = buildAccommodationTypeDto(
          code = prisonAccommodationTypeCode,
          description = prisoner.prisonName,
        ),
      )

      val result = AccommodationTransformer.toAccommodationSummary(
        crn = crn,
        prisoner = prisoner,
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `toAccommodationSummary() should map all fields when it is a cas 1 current premises`() {
      val crn = "X92123"

      val cas1CurrentPremises = buildCas1PremisesSummary()

      val expectedResult = buildAccommodationSummaryDto(
        crn = crn,
        startDate = cas1CurrentPremises.startDate,
        endDate = cas1CurrentPremises.endDate,
        address = buildAccommodationAddressDetails(
          subBuildingName = null,
          postcode = cas1CurrentPremises.postcode,
          buildingName = null,
          buildingNumber = null,
          thoroughfareName = cas1CurrentPremises.addressLine1,
          dependentLocality = cas1CurrentPremises.addressLine2,
          postTown = cas1CurrentPremises.town,
          county = null,
          country = null,
          uprn = null,
        ),
        status = buildAccommodationStatusDto(
          code = AddressStatusCode.M.name,
          description = AddressStatusCode.M.description,
        ),
        type = buildAccommodationTypeDto(
          code = AddressUsageCode.A02.name,
          description = AddressUsageCode.A02.description,
        ),
      )

      val result = AccommodationTransformer.toAccommodationSummary(
        crn = crn,
        premises = cas1CurrentPremises,
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `toAccommodationSummary() should map all fields when it is a cas 3 current premises`() {
      val crn = "X92123"

      val cas3CurrentPremises = buildCas3PremisesSummary()

      val expectedResult = buildAccommodationSummaryDto(
        crn = crn,
        startDate = cas3CurrentPremises.startDate,
        endDate = cas3CurrentPremises.endDate,
        address = buildAccommodationAddressDetails(
          subBuildingName = null,
          postcode = cas3CurrentPremises.postcode,
          buildingName = null,
          buildingNumber = null,
          thoroughfareName = cas3CurrentPremises.addressLine1,
          dependentLocality = cas3CurrentPremises.addressLine2,
          postTown = cas3CurrentPremises.town,
          county = null,
          country = null,
          uprn = null,
        ),
        status = buildAccommodationStatusDto(
          code = AddressStatusCode.M.name,
          description = AddressStatusCode.M.description,
        ),
        type = buildAccommodationTypeDto(
          code = AddressUsageCode.A17.name,
          description = AddressUsageCode.A17.description,
        ),
      )

      val result = AccommodationTransformer.toAccommodationSummary(
        crn = crn,
        premises = cas3CurrentPremises,
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `should return null status when addressStatus is null`() {
      val address = buildCanonicalAddress(
        status = CanonicalAddressStatus(
          code = null,
          description = null,
        ),
      )
      val result = AccommodationTransformer.toAccommodationSummary(
        crn = "X92123",
        address = address,
      )
      assertThat(result.status).isNull()
    }

    @Test
    fun `should map status when code present but description null`() {
      val address = buildCanonicalAddress(
        status = CanonicalAddressStatus(code = "M", description = null),
      )

      val result = AccommodationTransformer.toAccommodationSummary("X92123", address)

      assertThat(result.status).isNotNull()
      assertThat(result.status!!.code).isEqualTo("M")
      assertThat(result.status!!.description).isNull()
    }

    @Test
    fun `should return null type when no active usage`() {
      val address = buildCanonicalAddress(
        usages = listOf(
          CanonicalAddressUsage(
            usageCode = CanonicalAddressUsageCode("A01A", "desc"),
            isActive = false,
          ),
        ),
      )

      val result = AccommodationTransformer.toAccommodationSummary("X92123", address)

      assertThat(result.type).isNull()
    }

    @Test
    fun `should return null type when usage code is null`() {
      val address = buildCanonicalAddress(
        usages = listOf(
          CanonicalAddressUsage(
            usageCode = CanonicalAddressUsageCode(null, "desc"),
            isActive = true,
          ),
        ),
      )

      val result = AccommodationTransformer.toAccommodationSummary("X92123", address)

      assertThat(result.type).isNull()
    }

    @Test
    fun `should handle null address fields`() {
      val address = buildCanonicalAddress(
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

      val result = AccommodationTransformer.toAccommodationSummary(
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

    @Test
    fun `should map proposed accommodation entity correctly`() {
      val entity = buildProposedAccommodationEntity(
        cprAddressId = UUID.randomUUID(),
        typeVerified = true,
        noFixedAbode = true,
        createdAt = Instant.now(),
        startDate = LocalDate.of(2023, 1, 1),
        endDate = LocalDate.of(2024, 1, 1),
        postcode = "AB1 2CD",
        subBuildingName = "Flat 1",
        buildingName = "Test House",
        buildingNumber = "10",
        throughfareName = "High Street",
        dependentLocality = "Town Centre",
        postTown = "London",
        county = "Greater London",
        country = "England",
        uprn = "12345",
      )
      val type = buildAccommodationTypeEntity(
        code = AddressUsageCode.A01A.name,
        name = AddressUsageCode.A01A.description,
      )
      val status = buildAccommodationStatusEntity(
        code = AddressStatusCode.PR.name,
        name = AddressStatusCode.PR.description,
      )

      val result = AccommodationTransformer.toAccommodationSummary(
        crn = "X123",
        entity,
        type,
        status,
      )

      assertThat(result.crn).isEqualTo("X123")
      assertThat(result.startDate).isNull()
      assertThat(result.endDate).isNull()
      assertThat(result.status!!.code).isEqualTo(AddressStatusCode.PR.name)
      assertThat(result.status!!.description).isEqualTo(AddressStatusCode.PR.description)
      assertThat(result.type!!.code).isEqualTo(AddressUsageCode.A01A.name)
      assertThat(result.type!!.description).isEqualTo(AddressUsageCode.A01A.description)
      assertThat(result.address.postcode).isEqualTo(entity.postcode)
      assertThat(result.address.subBuildingName).isEqualTo(entity.subBuildingName)
      assertThat(result.address.buildingName).isEqualTo(entity.buildingName)
      assertThat(result.address.buildingNumber).isEqualTo(entity.buildingNumber)
      assertThat(result.address.thoroughfareName).isEqualTo(entity.throughfareName)
      assertThat(result.address.dependentLocality).isEqualTo(entity.dependentLocality)
      assertThat(result.address.postTown).isEqualTo(entity.postTown)
      assertThat(result.address.county).isEqualTo(entity.county)
      assertThat(result.address.country).isEqualTo(entity.country)
      assertThat(result.address.uprn).isEqualTo(entity.uprn)
    }
  }

  @Nested
  inner class ToAccommodationDetail {

    @Test
    fun `should map proposed accommodation entity correctly`() {
      val entity = buildProposedAccommodationEntity(
        cprAddressId = UUID.randomUUID(),
        typeVerified = true,
        noFixedAbode = true,
        createdAt = Instant.now(),
        startDate = LocalDate.of(2023, 1, 1),
        endDate = LocalDate.of(2024, 1, 1),
        postcode = "AB1 2CD",
        subBuildingName = "Flat 1",
        buildingName = "Test House",
        buildingNumber = "10",
        throughfareName = "High Street",
        dependentLocality = "Town Centre",
        postTown = "London",
        county = "Greater London",
        country = "England",
        uprn = "12345",
      )
      val type = buildAccommodationTypeEntity(
        code = AddressUsageCode.A01A.name,
        name = AddressUsageCode.A01A.description,
      )
      val status = buildAccommodationStatusEntity(
        code = AddressStatusCode.M.name,
        name = AddressStatusCode.M.description,
      )

      val result = AccommodationTransformer.toAccommodationDetail(
        crn = "X123",
        entity,
        type,
        status,
      )

      assertThat(result.crn).isEqualTo("X123")
      assertThat(result.cprAddressId).isEqualTo(entity.cprAddressId)
      assertThat(result.typeVerified).isEqualTo(entity.typeVerified)
      assertThat(result.noFixedAbode).isEqualTo(entity.noFixedAbode)
      assertThat(result.startDate).isEqualTo(entity.createdAt!!.atZone(ZoneId.systemDefault()).toLocalDate())
      assertThat(result.endDate).isNull()
      assertThat(result.status!!.code).isEqualTo(AddressStatusCode.M.name)
      assertThat(result.status!!.description).isEqualTo(AddressStatusCode.M.description)
      assertThat(result.type!!.code).isEqualTo(AddressUsageCode.A01A.name)
      assertThat(result.type!!.description).isEqualTo(AddressUsageCode.A01A.description)
      assertThat(result.address.postcode).isEqualTo(entity.postcode)
      assertThat(result.address.subBuildingName).isEqualTo(entity.subBuildingName)
      assertThat(result.address.buildingName).isEqualTo(entity.buildingName)
      assertThat(result.address.buildingNumber).isEqualTo(entity.buildingNumber)
      assertThat(result.address.thoroughfareName).isEqualTo(entity.throughfareName)
      assertThat(result.address.dependentLocality).isEqualTo(entity.dependentLocality)
      assertThat(result.address.postTown).isEqualTo(entity.postTown)
      assertThat(result.address.county).isEqualTo(entity.county)
      assertThat(result.address.country).isEqualTo(entity.country)
      assertThat(result.address.uprn).isEqualTo(entity.uprn)
    }

    @Test
    fun `should handle null status entity`() {
      val entity = buildProposedAccommodationEntity(
        cprAddressId = UUID.randomUUID(),
        createdAt = Instant.now(),
      )
      val type = buildAccommodationTypeEntity(
        code = AddressUsageCode.A01A.name,
        name = AddressUsageCode.A01A.description,
      )
      val result = AccommodationTransformer.toAccommodationDetail(
        crn = "X123",
        entity,
        type,
        null,
      )
      assertThat(result.status).isNull()
      assertThat(result.type!!.code).isEqualTo(AddressUsageCode.A01A.name)
      assertThat(result.type!!.description).isEqualTo(AddressUsageCode.A01A.description)
    }

    @Test
    fun `should map canonical address correctly`() {
      val cprAddressId = UUID.randomUUID()
      val address = buildCanonicalAddress(
        cprAddressId = cprAddressId,
        typeVerified = true,
        noFixedAbode = true,
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
        status = CanonicalAddressStatus(
          code = AddressStatusCode.P.name,
          description = AddressStatusCode.P.description,
        ),
        usages = listOf(
          CanonicalAddressUsage(
            usageCode = CanonicalAddressUsageCode(
              code = AddressUsageCode.A01A.name,
              description = AddressUsageCode.A01A.description,
            ),
            isActive = true,
          ),
        ),
        uprn = "100012345678",
      )

      val result = toAccommodationDetail(
        crn = "X92123",
        address = address,
      )

      assertThat(result.crn).isEqualTo("X92123")
      assertThat(result.cprAddressId).isEqualTo(cprAddressId)
      assertThat(result.typeVerified).isTrue
      assertThat(result.noFixedAbode).isTrue
      assertThat(result.startDate).isEqualTo(LocalDate.of(2023, 1, 1))
      assertThat(result.endDate).isEqualTo(LocalDate.of(2024, 1, 1))
      assertThat(result.status!!.code).isEqualTo(AddressStatusCode.P.name)
      assertThat(result.status!!.description).isEqualTo(AddressStatusCode.P.description)
      assertThat(result.type!!.code).isEqualTo(AddressUsageCode.A01A.name)
      assertThat(result.type!!.description).isEqualTo(AddressUsageCode.A01A.description)
      assertThat(result.address.postcode).isEqualTo(address.postcode)
      assertThat(result.address.subBuildingName).isEqualTo(address.subBuildingName)
      assertThat(result.address.buildingName).isEqualTo(address.buildingName)
      assertThat(result.address.buildingNumber).isEqualTo(address.buildingNumber)
      assertThat(result.address.thoroughfareName).isEqualTo(address.thoroughfareName)
      assertThat(result.address.dependentLocality).isEqualTo(address.dependentLocality)
      assertThat(result.address.postTown).isEqualTo(address.postTown)
      assertThat(result.address.county).isEqualTo(address.county)
      assertThat(result.address.country).isEqualTo(null)
      assertThat(result.address.uprn).isEqualTo(address.uprn)
    }

    @Test
    fun `should return null status and type when canonical address has null status code and no active usage`() {
      val address = buildCanonicalAddress(
        cprAddressId = UUID.randomUUID(),
        status = CanonicalAddressStatus(
          code = null,
          description = null,
        ),
        usages = listOf(
          CanonicalAddressUsage(
            usageCode = CanonicalAddressUsageCode(
              code = AddressUsageCode.A01A.name,
              description = AddressUsageCode.A01A.description,
            ),
            isActive = false,
          ),
        ),
      )

      val result = toAccommodationDetail(
        crn = "X92123",
        address = address,
      )

      assertThat(result.status).isNull()
      assertThat(result.type).isNull()
    }
  }
}
