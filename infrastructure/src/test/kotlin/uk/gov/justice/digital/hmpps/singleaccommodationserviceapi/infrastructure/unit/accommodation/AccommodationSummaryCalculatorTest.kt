package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.accommodation

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.accommodation.AccommodationSummaryCalculator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.InOutStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PlacementSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3LatestBooking
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3SubmittedApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPrisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import java.time.LocalDate
import java.util.UUID
import java.util.stream.Stream
typealias CaseAccommodationScenario = CaseAccommodationStatusScenarioLoader.Scenario

@ExtendWith(MockKExtension::class)
class AccommodationSummaryCalculatorTest {

  @MockK
  lateinit var accommodationTypeRepository: AccommodationTypeRepository

  @MockK
  lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  @InjectMockKs
  lateinit var calculator: AccommodationSummaryCalculator

  private val crn = "X12345"

  private fun buildAccommodationTypeEntity(
    code: String,
    settledType: AccommodationSettledType = AccommodationSettledType.TRANSIENT,
    isHomeless: Boolean = false,
  ) = AccommodationTypeEntity(
    id = UUID.randomUUID(),
    name = code,
    code = code,
    settledType = settledType,
    active = true,
    isProposed = false,
    isPrivate = false,
    isPrison = false,
    isCas1 = false,
    isCas2 = false,
    isHomeless = isHomeless,
  )

  private fun buildAddress(
    statusCode: String? = AddressStatusCode.M.name,
    usageCode: String? = AddressUsageCode.A07B.name,
    postcode: String? = "SW1A 1AA",
    endDate: String? = null,
  ) = CanonicalAddress(
    cprAddressId = UUID.randomUUID().toString(),
    postcode = postcode,
    endDate = endDate,
    status = CanonicalAddressStatus(code = statusCode),
    usages = listOf(
      CanonicalAddressUsage(
        usageCode = CanonicalAddressUsageCode(code = usageCode),
        isActive = true,
      ),
    ),
  )

  @BeforeEach
  fun setup() {
    every { accommodationTypeRepository.findAllBySettledTypeAndActiveIsTrue(AccommodationSettledType.TRANSIENT) } returns
      listOf(buildAccommodationTypeEntity(code = "A03", settledType = AccommodationSettledType.TRANSIENT))
    every { accommodationTypeRepository.findAllBySettledTypeAndActiveIsTrue(AccommodationSettledType.SETTLED) } returns
      listOf(buildAccommodationTypeEntity(code = "A01A", settledType = AccommodationSettledType.SETTLED))
    every { accommodationTypeRepository.findAllByIsHomelessIsTrueAndActiveIsTrue() } returns
      listOf(buildAccommodationTypeEntity(code = "A08", isHomeless = true))
    every { proposedAccommodationRepository.findByCprAddressId(any()) } returns null
  }

  @Nested
  inner class CalculateCurrentAccommodation {
    @Test
    fun `returns prison when prisoner is in custody`() {
      val prisoner = buildPrisoner(inOutStatus = InOutStatus.IN, prisonName = "HMP Test")

      val result = calculator.calculateCurrentAccommodation(
        crn = crn,
        addresses = listOf(buildAddress()),
        prisoner = prisoner,
        cas1CurrentPremises = null,
        cas3CurrentPremises = null,
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
          code = "HMP",
          description = prisoner.prisonName,
        ),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `returns CAS1 premises when main address has A02 usage and matching postcode`() {
      val postcode = "SW1A 1AA"
      val cas1CurrentPremises = buildCas1PremisesSummary(postcode = postcode)
      val mainAddress = buildCanonicalAddress(
        cprAddressId = null,
        postcode = postcode,
        thoroughfareName = "Some Street",
        postTown = "London",
        status = CanonicalAddressStatus(code = AddressStatusCode.M.name, description = AddressStatusCode.M.description),
        usages = listOf(
          CanonicalAddressUsage(
            usageCode = CanonicalAddressUsageCode(code = AddressUsageCode.A02.name, description = AddressUsageCode.A02.description),
            isActive = true,
          ),
        ),
      )

      val result = calculator.calculateCurrentAccommodation(
        crn = crn,
        addresses = listOf(mainAddress),
        prisoner = null,
        cas1CurrentPremises = cas1CurrentPremises,
        cas3CurrentPremises = null,
      )

      val expectedResult = buildAccommodationSummaryDto(
        crn = crn,
        startDate = cas1CurrentPremises.startDate,
        endDate = cas1CurrentPremises.endDate,
        address = buildAccommodationAddressDetails(
          subBuildingName = mainAddress.subBuildingName,
          postcode = postcode,
          buildingName = mainAddress.buildingName,
          buildingNumber = mainAddress.buildingNumber,
          thoroughfareName = mainAddress.thoroughfareName,
          dependentLocality = mainAddress.dependentLocality,
          postTown = mainAddress.postTown,
          county = mainAddress.county,
          country = mainAddress.countryCode,
          uprn = mainAddress.uprn,
        ),
        status = buildAccommodationStatusDto(code = AddressStatusCode.M.name, description = AddressStatusCode.M.description),
        type = buildAccommodationTypeDto(code = AddressUsageCode.A02.name, description = AddressUsageCode.A02.description),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `returns CAS1 premises without end dates when postcode does not match`() {
      val cas1CurrentPremises = buildCas1PremisesSummary(postcode = "SW1A 1AA")
      val mainAddress = buildCanonicalAddress(
        cprAddressId = UUID.randomUUID(),
        postcode = "SW1A 1AB",
        thoroughfareName = "Some Street",
        postTown = "London",
        status = CanonicalAddressStatus(code = AddressStatusCode.M.name, description = AddressStatusCode.M.description),
        usages = listOf(
          CanonicalAddressUsage(
            usageCode = CanonicalAddressUsageCode(code = AddressUsageCode.A02.name, description = AddressUsageCode.A02.description),
            isActive = true,
          ),
        ),
      )

      val result = calculator.calculateCurrentAccommodation(
        crn = crn,
        addresses = listOf(mainAddress),
        prisoner = null,
        cas1CurrentPremises = cas1CurrentPremises,
        cas3CurrentPremises = null,
      )

      val expectedResult = buildAccommodationSummaryDto(
        crn = crn,
        startDate = LocalDate.parse(requireNotNull(mainAddress.startDate)),
        endDate = null,
        address = buildAccommodationAddressDetails(
          subBuildingName = mainAddress.subBuildingName,
          postcode = "SW1A 1AB",
          buildingName = mainAddress.buildingName,
          buildingNumber = mainAddress.buildingNumber,
          thoroughfareName = mainAddress.thoroughfareName,
          dependentLocality = mainAddress.dependentLocality,
          postTown = mainAddress.postTown,
          county = mainAddress.county,
          country = mainAddress.countryCode,
          uprn = mainAddress.uprn,
        ),
        status = buildAccommodationStatusDto(code = AddressStatusCode.M.name, description = AddressStatusCode.M.description),
        type = buildAccommodationTypeDto(code = AddressUsageCode.A02.name, description = AddressUsageCode.A02.description),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `returns CAS3 premises when main address has A17 usage and matching postcode`() {
      val postcode = "SW1A 1AA"
      val cas3CurrentPremises = buildCas3PremisesSummary(postcode = postcode)
      val mainAddress = buildCanonicalAddress(
        cprAddressId = null,
        postcode = postcode,
        thoroughfareName = "Some Street",
        postTown = "London",
        status = CanonicalAddressStatus(code = AddressStatusCode.M.name, description = AddressStatusCode.M.description),
        usages = listOf(
          CanonicalAddressUsage(
            usageCode = CanonicalAddressUsageCode(code = AddressUsageCode.A17.name, description = AddressUsageCode.A17.description),
            isActive = true,
          ),
        ),
      )

      val result = calculator.calculateCurrentAccommodation(
        crn = crn,
        addresses = listOf(mainAddress),
        prisoner = null,
        cas1CurrentPremises = null,
        cas3CurrentPremises = cas3CurrentPremises,
      )

      val expectedResult = buildAccommodationSummaryDto(
        crn = crn,
        startDate = cas3CurrentPremises.startDate,
        endDate = cas3CurrentPremises.endDate,
        address = buildAccommodationAddressDetails(
          subBuildingName = mainAddress.subBuildingName,
          postcode = postcode,
          buildingName = mainAddress.buildingName,
          buildingNumber = mainAddress.buildingNumber,
          thoroughfareName = mainAddress.thoroughfareName,
          dependentLocality = mainAddress.dependentLocality,
          postTown = mainAddress.postTown,
          county = mainAddress.county,
          country = mainAddress.countryCode,
          uprn = mainAddress.uprn,
        ),
        status = buildAccommodationStatusDto(code = AddressStatusCode.M.name, description = AddressStatusCode.M.description),
        type = buildAccommodationTypeDto(code = AddressUsageCode.A17.name, description = AddressUsageCode.A17.description),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `returns CAS3 premises without end dates when postcode does not match`() {
      val cas3CurrentPremises = buildCas3PremisesSummary(postcode = "SW1A 1AA")
      val mainAddress = buildCanonicalAddress(
        cprAddressId = UUID.randomUUID(),
        postcode = "SW1A 1AB",
        thoroughfareName = "Some Street",
        postTown = "London",
        status = CanonicalAddressStatus(code = AddressStatusCode.M.name, description = AddressStatusCode.M.description),
        usages = listOf(
          CanonicalAddressUsage(
            usageCode = CanonicalAddressUsageCode(code = AddressUsageCode.A17.name, description = AddressUsageCode.A17.description),
            isActive = true,
          ),
        ),
      )

      val result = calculator.calculateCurrentAccommodation(
        crn = crn,
        addresses = listOf(mainAddress),
        prisoner = null,
        cas1CurrentPremises = null,
        cas3CurrentPremises = cas3CurrentPremises,
      )

      val expectedResult = buildAccommodationSummaryDto(
        crn = crn,
        startDate = LocalDate.parse(requireNotNull(mainAddress.startDate)),
        endDate = null,
        address = buildAccommodationAddressDetails(
          subBuildingName = mainAddress.subBuildingName,
          postcode = "SW1A 1AB",
          buildingName = mainAddress.buildingName,
          buildingNumber = mainAddress.buildingNumber,
          thoroughfareName = mainAddress.thoroughfareName,
          dependentLocality = mainAddress.dependentLocality,
          postTown = mainAddress.postTown,
          county = mainAddress.county,
          country = mainAddress.countryCode,
          uprn = mainAddress.uprn,
        ),
        status = buildAccommodationStatusDto(code = AddressStatusCode.M.name, description = AddressStatusCode.M.description),
        type = buildAccommodationTypeDto(code = AddressUsageCode.A17.name, description = AddressUsageCode.A17.description),
      )

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `falls back to CPR main address when no CAS1 or CAS3 postcode match`() {
      val mainAddress = buildAddress(usageCode = AddressUsageCode.A07B.name, postcode = "SW1A 1AA")

      val result = calculator.calculateCurrentAccommodation(
        crn = crn,
        addresses = listOf(mainAddress),
        prisoner = null,
        cas1CurrentPremises = buildCas1PremisesSummary(postcode = "DIFFERENT"),
        cas3CurrentPremises = null,
      )

      assertThat(result?.type?.code).isEqualTo(AddressUsageCode.A07B.name)
      assertThat(result?.address?.postcode).isEqualTo("SW1A 1AA")
    }

    @Test
    fun `returns the CPR main address when there is no prisoner, CAS1 or CAS3 premises`() {
      val mainAddress = buildCanonicalAddress(
        cprAddressId = null,
        postcode = "SW1A 1AA",
        thoroughfareName = "Some Street",
        postTown = "London",
        status = CanonicalAddressStatus(code = AddressStatusCode.M.name, description = AddressStatusCode.M.description),
        usages = listOf(
          CanonicalAddressUsage(
            usageCode = CanonicalAddressUsageCode(code = AddressUsageCode.A01A.name, description = AddressUsageCode.A01A.description),
            isActive = true,
          ),
        ),
      )
      val previousAddress = buildCanonicalAddress(
        cprAddressId = null,
        postcode = "GL53 8GH",
        thoroughfareName = "",
        postTown = "Cheltenham",
        status = CanonicalAddressStatus(code = AddressStatusCode.P.name, description = AddressStatusCode.P.description),
        usages = listOf(
          CanonicalAddressUsage(
            usageCode = CanonicalAddressUsageCode(code = AddressUsageCode.A07A.name, description = AddressUsageCode.A07A.description),
            isActive = true,
          ),
        ),
      )

      val result = calculator.calculateCurrentAccommodation(
        crn = crn,
        addresses = listOf(mainAddress, previousAddress),
        prisoner = buildPrisoner(inOutStatus = InOutStatus.OUT, prisonName = "A Prison"),
        cas1CurrentPremises = null,
        cas3CurrentPremises = null,
      )

      assertThat(result?.address?.postcode).isEqualTo("SW1A 1AA")
      assertThat(result?.status!!.code).isEqualTo("M")
    }

    @Test
    fun `returns null when there is no prisoner and no main address`() {
      val result = calculator.calculateCurrentAccommodation(
        crn = crn,
        addresses = listOf(buildAddress(statusCode = AddressStatusCode.P.name)),
        prisoner = null,
        cas1CurrentPremises = null,
        cas3CurrentPremises = null,
      )

      assertThat(result).isNull()
    }
  }

  @Nested
  inner class CalculateNextAccommodations {
    @Test
    fun `returns CAS1 next accommodation when application placement status is UPCOMING`() {
      val cas1Application = buildCas1Application(
        placement = buildCas1PlacementSummary(
          status = Cas1PlacementStatus.UPCOMING,
          premises = buildCas1PremisesSummary(),
        ),
      )

      val result = calculator.calculateNextAccommodations(
        crn = crn,
        addresses = null,
        cas1Application = cas1Application,
        cas3Application = null,
        currentAccommodation = null,
      )

      assertThat(result).hasSize(1)
      assertThat(result.single().type?.code).isEqualTo(AddressUsageCode.A02.name)
    }

    @Test
    fun `returns CAS1 next accommodation with PR1 status when current accommodation is prison`() {
      val cas1Application = buildCas1Application(
        placement = buildCas1PlacementSummary(
          status = Cas1PlacementStatus.UPCOMING,
          premises = buildCas1PremisesSummary(),
        ),
      )
      val prisonCurrentAccommodation = buildAccommodationSummaryDto(
        crn = crn,
        type = buildAccommodationTypeDto(code = "HMP"),
      )

      val result = calculator.calculateNextAccommodations(
        crn = crn,
        addresses = null,
        cas1Application = cas1Application,
        cas3Application = null,
        currentAccommodation = prisonCurrentAccommodation,
      )

      assertThat(result.single().status?.code).isEqualTo(AddressStatusCode.PR1.name)
    }

    @Test
    fun `returns CAS1 next accommodation with PR status when current accommodation is not prison`() {
      val cas1Application = buildCas1Application(
        placement = buildCas1PlacementSummary(
          status = Cas1PlacementStatus.UPCOMING,
          premises = buildCas1PremisesSummary(),
        ),
      )
      val nonPrisonCurrentAccommodation = buildAccommodationSummaryDto(
        crn = crn,
        type = buildAccommodationTypeDto(code = AddressUsageCode.A01A.name),
      )

      val result = calculator.calculateNextAccommodations(
        crn = crn,
        addresses = null,
        cas1Application = cas1Application,
        cas3Application = null,
        currentAccommodation = nonPrisonCurrentAccommodation,
      )

      assertThat(result.single().status?.code).isEqualTo(AddressStatusCode.PR.name)
    }

    @Test
    fun `returns CAS3 next accommodation when application booking status is CONFIRMED`() {
      val cas3Application = buildCas3Application(
        applicationStatus = Cas3ApplicationStatus.SUBMITTED,
        submittedApplication = buildCas3SubmittedApplicationDto(
          latestBooking = buildCas3LatestBooking(
            status = Cas3BookingStatus.CONFIRMED,
            premises = buildCas3PremisesSummary(),
          ),
        ),
      )

      val result = calculator.calculateNextAccommodations(
        crn = crn,
        addresses = null,
        cas1Application = null,
        cas3Application = cas3Application,
        currentAccommodation = null,
      )

      assertThat(result).hasSize(1)
      assertThat(result.single().type?.code).isEqualTo(AddressUsageCode.A17.name)
    }

    @Test
    fun `ignores CAS1 application when placement status is not UPCOMING`() {
      val cas1Application = buildCas1Application(
        placement = buildCas1PlacementSummary(
          status = null,
          premises = buildCas1PremisesSummary(),
        ),
      )

      val result = calculator.calculateNextAccommodations(
        crn = crn,
        addresses = null,
        cas1Application = cas1Application,
        cas3Application = null,
        currentAccommodation = null,
      )

      assertThat(result).isEmpty()
    }

    @Test
    fun `ignores CAS3 application when booking status is not CONFIRMED`() {
      val cas3Application = buildCas3Application(
        applicationStatus = Cas3ApplicationStatus.SUBMITTED,
        submittedApplication = buildCas3SubmittedApplicationDto(
          latestBooking = buildCas3LatestBooking(
            status = Cas3BookingStatus.ARRIVED,
            premises = buildCas3PremisesSummary(),
          ),
        ),
      )

      val result = calculator.calculateNextAccommodations(
        crn = crn,
        addresses = null,
        cas1Application = null,
        cas3Application = cas3Application,
        currentAccommodation = null,
      )

      assertThat(result).isEmpty()
    }

    @Test
    fun `returns proposed CPR address as next accommodation when it has no end date`() {
      val proposedAddress = buildAddress(statusCode = AddressStatusCode.PR.name, postcode = "SW1A 1AA", endDate = null)
      val equivalentProposedAccommodationEntity = buildProposedAccommodationEntity()

      every { proposedAccommodationRepository.findByCprAddressId(UUID.fromString(proposedAddress.cprAddressId)) } returns equivalentProposedAccommodationEntity

      val result = calculator.calculateNextAccommodations(
        crn = crn,
        addresses = listOf(proposedAddress),
        cas1Application = null,
        cas3Application = null,
        currentAccommodation = null,
      )

      assertThat(result).hasSize(1)
      assertThat(result.single().startDate).isNull()
      assertThat(result.single().endDate).isNull()
      assertThat(result.single().proposedAccommodationId).isEqualTo(equivalentProposedAccommodationEntity.id)
    }

    @Test
    fun `is empty when only proposed accommodation has an end date`() {
      val withEndDate = buildAddress(statusCode = AddressStatusCode.PR.name, postcode = "SW1A 1AA", endDate = "2020-01-01")

      val result = calculator.calculateNextAccommodations(
        crn = crn,
        addresses = listOf(withEndDate),
        cas1Application = null,
        cas3Application = null,
        currentAccommodation = null,
      )

      assertThat(result).isEmpty()
    }

    @Test
    fun `is empty when only proposed accommodation has no postcode`() {
      val blankPostcode = buildAddress(statusCode = AddressStatusCode.PR1.name, postcode = "", endDate = null)

      val result = calculator.calculateNextAccommodations(
        crn = crn,
        addresses = listOf(blankPostcode),
        cas1Application = null,
        cas3Application = null,
        currentAccommodation = null,
      )

      assertThat(result).isEmpty()
    }

    @Test
    fun `prefers CAS1 or CAS3 next accommodation over a proposed CPR address`() {
      val cas1Application = buildCas1Application(
        placement = buildCas1PlacementSummary(
          status = Cas1PlacementStatus.UPCOMING,
          premises = buildCas1PremisesSummary(),
        ),
      )
      val proposedAddress = buildAddress(statusCode = AddressStatusCode.PR.name, postcode = "SW1A 1AA", endDate = null)
      val equivalentProposedAccommodationEntity = buildProposedAccommodationEntity()

      every { proposedAccommodationRepository.findByCprAddressId(UUID.fromString(proposedAddress.cprAddressId)) } returns equivalentProposedAccommodationEntity

      val result = calculator.calculateNextAccommodations(
        crn = crn,
        addresses = listOf(proposedAddress),
        cas1Application = cas1Application,
        cas3Application = null,
        currentAccommodation = null,
      )

      assertThat(result).hasSize(2)
      assertThat(result.first().type?.code).isEqualTo(AddressUsageCode.A02.name)
      assertThat(result.first().proposedAccommodationId).isNull()
    }

    @Test
    fun `returns CAS1, CAS3 and proposed CPR next accommodations together in that order`() {
      val proposedAddressId = UUID.randomUUID()
      val proposedAccommodationEntity = buildProposedAccommodationEntity(id = proposedAddressId)
      every { proposedAccommodationRepository.findByCprAddressId(proposedAddressId) } returns proposedAccommodationEntity

      val addresses = listOf(
        buildAddress(statusCode = AddressStatusCode.M.name, usageCode = AddressUsageCode.A01A.name, postcode = "SW1A 1AA"),
        buildAddress(statusCode = AddressStatusCode.PR.name, postcode = "GL53 8GH", endDate = null).copy(cprAddressId = proposedAddressId.toString()),
      )
      val cas1Application = buildCas1Application(
        placement = buildCas1PlacementSummary(
          status = Cas1PlacementStatus.UPCOMING,
          premises = buildCas1PremisesSummary(postcode = "SW1A 1AB"),
        ),
      )
      val cas3Application = buildCas3Application(
        applicationStatus = Cas3ApplicationStatus.SUBMITTED,
        submittedApplication = buildCas3SubmittedApplicationDto(
          latestBooking = buildCas3LatestBooking(
            status = Cas3BookingStatus.CONFIRMED,
            premises = buildCas3PremisesSummary(postcode = "SW1A 1A4"),
          ),
        ),
      )

      val result = calculator.calculateNextAccommodations(
        crn = crn,
        addresses = addresses,
        cas1Application = cas1Application,
        cas3Application = cas3Application,
        currentAccommodation = null,
      )

      assertThat(result).hasSize(3)
      assertThat(result[0].address.postcode).isEqualTo("SW1A 1AB")
      assertThat(result[0].proposedAccommodationId).isNull()

      assertThat(result[1].address.postcode).isEqualTo("SW1A 1A4")
      assertThat(result[1].proposedAccommodationId).isNull()

      assertThat(result[2].address.postcode).isEqualTo("GL53 8GH")
      assertThat(result[2].proposedAccommodationId).isEqualTo(proposedAddressId)
    }

    @Test
    fun `returns CAS3 and proposed CPR next accommodations when CAS1 is not upcoming`() {
      val proposedAddressId = UUID.randomUUID()
      val proposedAccommodationEntity = buildProposedAccommodationEntity(id = proposedAddressId)
      every { proposedAccommodationRepository.findByCprAddressId(proposedAddressId) } returns proposedAccommodationEntity

      val addresses = listOf(
        buildAddress(statusCode = AddressStatusCode.PR.name, postcode = "GL53 8GH", endDate = null).copy(cprAddressId = proposedAddressId.toString()),
      )
      val cas1Application = buildCas1Application(
        placement = buildCas1PlacementSummary(
          status = Cas1PlacementStatus.ARRIVED,
          premises = buildCas1PremisesSummary(postcode = "SW1A 1AB"),
        ),
      )
      val cas3Application = buildCas3Application(
        applicationStatus = Cas3ApplicationStatus.SUBMITTED,
        submittedApplication = buildCas3SubmittedApplicationDto(
          latestBooking = buildCas3LatestBooking(
            status = Cas3BookingStatus.CONFIRMED,
            premises = buildCas3PremisesSummary(postcode = "SW1A 1A4"),
          ),
        ),
      )

      val result = calculator.calculateNextAccommodations(
        crn = crn,
        addresses = addresses,
        cas1Application = cas1Application,
        cas3Application = cas3Application,
        currentAccommodation = null,
      )

      assertThat(result).hasSize(2)
      assertThat(result[0].address.postcode).isEqualTo("SW1A 1A4")
      assertThat(result[0].proposedAccommodationId).isNull()

      assertThat(result[1].address.postcode).isEqualTo("GL53 8GH")
      assertThat(result[1].proposedAccommodationId).isEqualTo(proposedAddressId)
    }

    @Test
    fun `returns CAS1 and proposed CPR next accommodations when CAS3 is not confirmed`() {
      val proposedAddressId = UUID.randomUUID()
      val proposedAccommodationEntity = buildProposedAccommodationEntity(id = proposedAddressId)
      every { proposedAccommodationRepository.findByCprAddressId(proposedAddressId) } returns proposedAccommodationEntity

      val addresses = listOf(
        buildAddress(statusCode = AddressStatusCode.PR.name, postcode = "GL53 8GH", endDate = null).copy(cprAddressId = proposedAddressId.toString()),
      )
      val cas1Application = buildCas1Application(
        placement = buildCas1PlacementSummary(
          status = Cas1PlacementStatus.UPCOMING,
          premises = buildCas1PremisesSummary(postcode = "SW1A 1AB"),
        ),
      )
      val cas3Application = buildCas3Application(
        applicationStatus = Cas3ApplicationStatus.SUBMITTED,
        submittedApplication = buildCas3SubmittedApplicationDto(
          latestBooking = buildCas3LatestBooking(
            status = Cas3BookingStatus.ARRIVED,
            premises = buildCas3PremisesSummary(postcode = "SW1A 1A4"),
          ),
        ),
      )

      val result = calculator.calculateNextAccommodations(
        crn = crn,
        addresses = addresses,
        cas1Application = cas1Application,
        cas3Application = cas3Application,
        currentAccommodation = null,
      )

      assertThat(result).hasSize(2)
      assertThat(result[0].address.postcode).isEqualTo("SW1A 1AB")
      assertThat(result[0].proposedAccommodationId).isNull()

      assertThat(result[1].address.postcode).isEqualTo("GL53 8GH")
      assertThat(result[1].proposedAccommodationId).isEqualTo(proposedAddressId)
    }

    @Test
    fun `returns only the proposed CPR next accommodation when neither CAS1 nor CAS3 apply`() {
      val proposedAddressId = UUID.randomUUID()
      val proposedAccommodationEntity = buildProposedAccommodationEntity(id = proposedAddressId)
      every { proposedAccommodationRepository.findByCprAddressId(proposedAddressId) } returns proposedAccommodationEntity

      val addresses = listOf(
        buildAddress(statusCode = AddressStatusCode.PR.name, postcode = "GL53 8GH", endDate = null).copy(cprAddressId = proposedAddressId.toString()),
      )
      val cas1Application = buildCas1Application(
        placement = buildCas1PlacementSummary(
          status = Cas1PlacementStatus.ARRIVED,
          premises = buildCas1PremisesSummary(postcode = "SW1A 1AB"),
        ),
      )
      val cas3Application = buildCas3Application(
        applicationStatus = Cas3ApplicationStatus.SUBMITTED,
        submittedApplication = buildCas3SubmittedApplicationDto(
          latestBooking = buildCas3LatestBooking(
            status = Cas3BookingStatus.ARRIVED,
            premises = buildCas3PremisesSummary(postcode = "SW1A 1A4"),
          ),
        ),
      )

      val result = calculator.calculateNextAccommodations(
        crn = crn,
        addresses = addresses,
        cas1Application = cas1Application,
        cas3Application = cas3Application,
        currentAccommodation = null,
      )

      assertThat(result).hasSize(1)
      assertThat(result.single().address.postcode).isEqualTo("GL53 8GH")
      assertThat(result.single().proposedAccommodationId).isEqualTo(proposedAddressId)
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  inner class CalculateCaseAccommodationStatus {
    private val allCaseAccommodationStatusScenarios: List<CaseAccommodationScenario> by lazy {
      CaseAccommodationStatusScenarioLoader.loadScenarios()
    }

    fun caseAccommodationStatusScenarios(): Stream<Arguments> = allCaseAccommodationStatusScenarios
      .map { Arguments.of(it) }
      .stream()

    private fun buildAccommodation(typeCode: String?, endDate: String?): AccommodationSummaryDto? {
      val code = typeCode.nullIfBlank()?.toAccommodationTypeCode() ?: return null

      return buildAccommodationSummaryDto(
        type = buildAccommodationTypeDto(code = code),
        endDate = endDate.nullIfBlank()?.let(LocalDate::parse),
      )
    }

    private fun String?.nullIfBlank(): String? = this?.takeIf { it.isNotBlank() }

    private fun String.toAccommodationTypeCode(): String = when (this) {
      "HOMELESS" -> "A08"
      "SETTLED" -> "A01A"
      "TRANSIENT" -> "A03"
      "UNKNOWN" -> "UNKNOWN_CODE"
      else -> error("Unknown accommodation type name in CSV: $this")
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("caseAccommodationStatusScenarios")
    fun `returns the expected case accommodation status`(scenario: CaseAccommodationScenario) {
      val result = calculator.calculateCaseAccommodationStatus(
        currentAccommodation = buildAccommodation(scenario.currentType, scenario.currentEndDate),
        nextAccommodation = buildAccommodation(scenario.nextType, scenario.nextEndDate),
      )

      assertThat(result)
        .describedAs("Scenario %s (%s)", scenario.id, scenario.testName)
        .isEqualTo(scenario.expectedStatus)
    }
  }
}
