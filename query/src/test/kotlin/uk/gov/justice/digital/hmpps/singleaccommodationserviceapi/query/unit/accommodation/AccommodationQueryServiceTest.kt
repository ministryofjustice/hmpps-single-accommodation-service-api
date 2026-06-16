package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.accommodation

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.InOutStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationStatusEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPrisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationStatusRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildAccommodationOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildUpstreamFailure
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@ExtendWith(MockKExtension::class)
class AccommodationQueryServiceTest {
  @MockK
  lateinit var accommodationOrchestrationService: AccommodationOrchestrationService

  @MockK
  lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  @MockK
  lateinit var accommodationTypeRepository: AccommodationTypeRepository

  @MockK
  lateinit var accommodationStatusRepository: AccommodationStatusRepository

  @MockK
  lateinit var caseRepository: CaseRepository

  @InjectMockKs
  lateinit var accommodationQueryService: AccommodationQueryService

  private val crn = "X12345"
  private val prisonNumber = "12345"
  private val caseId = UUID.randomUUID()
  private val prisonAccommodationTypeCode = "HMP"

  @Nested
  inner class GetAccommodationHistory {
    @Test
    fun `getAccommodationHistory should orchestrate calls and map addresses`() {
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      every { caseRepository.findByCrn(crn) } returns caseEntity
      every { accommodationOrchestrationService.getCprAndPrisonOrchestration(crn, prisonNumber) } returns OrchestrationResultDto(
        data = buildAccommodationOrchestrationDto(
          cpr = buildCorePersonRecord(
            identifiers = buildIdentifiers(crns = listOf(crn)),
            addresses = listOf(
              buildCanonicalAddress(
                cprAddressId = UUID.randomUUID(),
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
              buildCanonicalAddress(
                cprAddressId = UUID.randomUUID(),
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
              buildCanonicalAddress(
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
    fun `getAccommodationHistory should orchestrate calls and map addresses and include prison at top if in prison`() {
      val prisoner = buildPrisoner(prisonNumber = prisonNumber, inOutStatus = InOutStatus.IN, prisonName = "A Prison")
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      every { caseRepository.findByCrn(crn) } returns caseEntity
      every { accommodationOrchestrationService.getCprAndPrisonOrchestration(crn, prisonNumber) } returns OrchestrationResultDto(
        data = buildAccommodationOrchestrationDto(
          prisoner = prisoner,
          cpr = buildCorePersonRecord(
            identifiers = buildIdentifiers(crns = listOf(crn)),
            addresses = listOf(
              buildCanonicalAddress(
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
              buildCanonicalAddress(
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
              buildCanonicalAddress(
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

      assertThat(result.data.size).isEqualTo(3)
      assertThat(result.data[0].address.buildingName).isEqualTo(prisoner.prisonName)
      assertThat(result.data[0].status!!.code).isEqualTo("C")
      assertThat(result.data[1].address.postcode).isEqualTo("SW1A 1AA")
      assertThat(result.data[1].status!!.code).isEqualTo(AddressStatusCode.M.name)
      assertThat(result.data[2].address.postcode).isNull()
      assertThat(result.data[2].status!!.code).isEqualTo(AddressStatusCode.P.name)
      assertThat(result.upstreamFailures.size).isEqualTo(0)
    }

    @Test
    fun `getAccommodationHistory should return empty list when cpr addresses call fails`() {
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      every { caseRepository.findByCrn(crn) } returns caseEntity
      every { accommodationOrchestrationService.getCprAndPrisonOrchestration(crn, prisonNumber) } returns OrchestrationResultDto(
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
  }

  @Nested
  inner class GetCurrentAccommodation {
    @Test
    fun `getCurrentAccommodation should get the current accommodation when in prison`() {
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      every { caseRepository.findByCrn(crn) } returns caseEntity
      val prisoner = buildPrisoner(prisonNumber = prisonNumber, inOutStatus = InOutStatus.IN, prisonName = "A Prison")
      val cas1CurrentPremises = buildCas1PremisesSummary()
      val cas3CurrentPremises = buildCas3PremisesSummary()
      val cpr = buildCorePersonRecord(
        identifiers = buildIdentifiers(prisonNumbers = listOf(prisonNumber)),
        addresses = listOf(
          buildCanonicalAddress(
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
          buildCanonicalAddress(
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

      val result = accommodationQueryService.getCurrentAccommodation(crn, cpr.addresses, prisoner, cas1CurrentPremises, cas3CurrentPremises)

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `getCurrentAccommodation should get the current accommodation when in cas1`() {
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      every { caseRepository.findByCrn(crn) } returns caseEntity
      val cas1CurrentPremises = buildCas1PremisesSummary()
      val cas3CurrentPremises = buildCas3PremisesSummary()
      val prisoner = buildPrisoner(prisonNumber = prisonNumber, inOutStatus = InOutStatus.OUT, prisonName = "A Prison")
      val cpr = buildCorePersonRecord(
        identifiers = buildIdentifiers(prisonNumbers = listOf(prisonNumber)),
        addresses = listOf(
          buildCanonicalAddress(
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
          buildCanonicalAddress(
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
      )

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

      val result = accommodationQueryService.getCurrentAccommodation(crn, cpr.addresses, prisoner, cas1CurrentPremises, cas3CurrentPremises)

      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `getCurrentAccommodation should get the current accommodation when in cas3`() {
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      every { caseRepository.findByCrn(crn) } returns caseEntity
      val cas3CurrentPremises = buildCas3PremisesSummary(postcode = "HELLO WORLD")
      val prisoner = buildPrisoner(prisonNumber = prisonNumber, inOutStatus = InOutStatus.OUT, prisonName = "A Prison")
      val cpr = buildCorePersonRecord(
        identifiers = buildIdentifiers(prisonNumbers = listOf(prisonNumber)),
        addresses = listOf(
          buildCanonicalAddress(
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
          buildCanonicalAddress(
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
      )

      val result = accommodationQueryService.getCurrentAccommodation(crn, cpr.addresses, prisoner, null, cas3CurrentPremises)

      assertThat(result?.address?.postcode).isEqualTo(cas3CurrentPremises.postcode)
      assertThat(result?.status!!.code).isEqualTo("M")
    }

    @Test
    fun `getCurrentAccommodation should get the current accommodation when not in prison or cas1 or cas3`() {
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      every { caseRepository.findByCrn(crn) } returns caseEntity
      val prisoner = buildPrisoner(prisonNumber = prisonNumber, inOutStatus = InOutStatus.OUT, prisonName = "A Prison")
      val cpr = buildCorePersonRecord(
        identifiers = buildIdentifiers(prisonNumbers = listOf(prisonNumber)),
        addresses = listOf(
          buildCanonicalAddress(
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
          buildCanonicalAddress(
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
      )

      val result = accommodationQueryService.getCurrentAccommodation(crn, cpr.addresses, prisoner, null, null)

      assertThat(result?.address?.postcode).isEqualTo("SW1A 1AA")
      assertThat(result?.status!!.code).isEqualTo("M")
    }

    @Test
    fun `getCurrentAccommodation should orchestrate calls and get the current accommodation`() {
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      every { caseRepository.findByCrn(crn) } returns caseEntity
      every { accommodationOrchestrationService.getAccommodationOrchestration(crn, prisonNumber) } returns OrchestrationResultDto(
        data = buildAccommodationOrchestrationDto(
          cpr = buildCorePersonRecord(
            addresses = listOf(
              buildCanonicalAddress(
                cprAddressId = UUID.randomUUID(),
                typeVerified = true,
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
              buildCanonicalAddress(
                cprAddressId = UUID.randomUUID(),
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
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      every { caseRepository.findByCrn(crn) } returns caseEntity
      every { accommodationOrchestrationService.getAccommodationOrchestration(crn, prisonNumber) } returns OrchestrationResultDto(
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

  @Nested
  inner class GetCurrentAndAllAccommodations {
    @Test
    fun `getCurrentAndAllAccommodations should orchestrate calls and return current accommodation and all accommodations`() {
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      every { caseRepository.findByCrn(crn) } returns caseEntity
      every {
        accommodationOrchestrationService.getAccommodationOrchestration(
          crn,
          prisonNumber,
        )
      } returns OrchestrationResultDto(
        data = buildAccommodationOrchestrationDto(
          cpr = buildCorePersonRecord(
            addresses = listOf(
              buildCanonicalAddress(
                cprAddressId = UUID.randomUUID(),
                typeVerified = true,
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
              buildCanonicalAddress(
                cprAddressId = UUID.randomUUID(),
                noFixedAbode = false,
                postcode = "GL53 8GH",
                thoroughfareName = "Another Road",
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

      val result = accommodationQueryService.getCurrentAndAllAccommodations(crn)

      assertThat(result.data.first!!.address.postcode).isEqualTo("SW1A 1AA")
      assertThat(result.data.first!!.status!!.code).isEqualTo(AddressStatusCode.M.name)
      assertThat(result.data.second.size).isEqualTo(2)
      assertThat(result.data.second[0].address.postcode).isEqualTo("SW1A 1AA")
      assertThat(result.data.second[0].status!!.code).isEqualTo(AddressStatusCode.M.name)
      assertThat(result.data.second[1].address.postcode).isEqualTo("GL53 8GH")
      assertThat(result.data.second[1].status!!.code).isEqualTo(AddressStatusCode.P.name)
      assertThat(result.upstreamFailures.size).isEqualTo(0)
    }

    @Test
    fun `getCurrentAndAllAccommodations should include prison as current accommodation when in prison and return all cpr accommodations`() {
      val prisoner = buildPrisoner(prisonNumber = prisonNumber, inOutStatus = InOutStatus.IN, prisonName = "A Prison")
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      every { caseRepository.findByCrn(crn) } returns caseEntity
      every {
        accommodationOrchestrationService.getAccommodationOrchestration(
          crn,
          prisonNumber,
        )
      } returns OrchestrationResultDto(
        data = buildAccommodationOrchestrationDto(
          prisoner = prisoner,
          cpr = buildCorePersonRecord(
            addresses = listOf(
              buildCanonicalAddress(
                cprAddressId = UUID.randomUUID(),
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
            ),
          ),
        ),
        upstreamFailures = emptyList(),
      )

      val result = accommodationQueryService.getCurrentAndAllAccommodations(crn)

      assertThat(result.data.first!!.address.buildingName).isEqualTo(prisoner.prisonName)
      assertThat(result.data.first!!.status!!.code).isEqualTo("C")
      assertThat(result.data.second.size).isEqualTo(1)
      assertThat(result.data.second[0].address.postcode).isEqualTo("SW1A 1AA")
      assertThat(result.data.second[0].status!!.code).isEqualTo(AddressStatusCode.M.name)
      assertThat(result.upstreamFailures.size).isEqualTo(0)
    }

    @Test
    fun `getCurrentAndAllAccommodations should return null current accommodation and empty all accommodations when cpr is null`() {
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      every { caseRepository.findByCrn(crn) } returns caseEntity
      every {
        accommodationOrchestrationService.getAccommodationOrchestration(
          crn,
          prisonNumber,
        )
      } returns OrchestrationResultDto(
        data = buildAccommodationOrchestrationDto(
          cpr = null,
          prisoner = null,
        ),
        upstreamFailures = listOf(
          buildUpstreamFailure(
            callKey = ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN,
          ),
        ),
      )

      val result = accommodationQueryService.getCurrentAndAllAccommodations(crn)

      assertThat(result.data.first).isNull()
      assertThat(result.data.second).isEmpty()
      assertThat(result.upstreamFailures.first().endpoint).isEqualTo(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN)
    }
  }

  @Nested
  inner class GetNextAccommodation {
    @Test
    fun `getNextAccommodation should orchestrate calls and get the next accommodation when current accommodation is prison`() {
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      every { caseRepository.findByCrn(crn) } returns caseEntity
      every { accommodationOrchestrationService.getNextAccommodationOrchestration(crn, prisonNumber) } returns OrchestrationResultDto(
        data = buildAccommodationOrchestrationDto(
          cpr = buildCorePersonRecord(
            addresses = listOf(
              buildCanonicalAddress(
                cprAddressId = UUID.randomUUID(),
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
            ),
          ),
          cas1Application = buildCas1Application(
            placementStatus = Cas1PlacementStatus.UPCOMING,
            premises = buildCas1PremisesSummary(
              postcode = "SW1A 1AB",
            ),
          ),
          cas3Application = buildCas3Application(
            bookingStatus = Cas3BookingStatus.CONFIRMED,
            premises = buildCas3PremisesSummary(
              postcode = "SW1A 1A4",
            ),
          ),
          prisoner = buildPrisoner(prisonNumber = prisonNumber, inOutStatus = InOutStatus.IN, prisonName = "A Prison"),
        ),
        upstreamFailures = emptyList(),
      )

      val result = accommodationQueryService.getNextAccommodation(crn)
      assertThat(result.data!!.address.postcode).isEqualTo("SW1A 1AB")
      assertThat(result.data!!.status!!.code).isEqualTo("PR1")
    }

    @Test
    fun `getNextAccommodation should orchestrate calls and get the next accommodation is not prison`() {
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      every { caseRepository.findByCrn(crn) } returns caseEntity
      every { accommodationOrchestrationService.getNextAccommodationOrchestration(crn, prisonNumber) } returns OrchestrationResultDto(
        data = buildAccommodationOrchestrationDto(
          cpr = buildCorePersonRecord(
            addresses = listOf(
              buildCanonicalAddress(
                cprAddressId = UUID.randomUUID(),
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
            ),
          ),
          cas1Application = buildCas1Application(
            placementStatus = Cas1PlacementStatus.UPCOMING,
            premises = buildCas1PremisesSummary(
              postcode = "SW1A 1AB",
            ),
          ),
          cas3Application = buildCas3Application(
            bookingStatus = Cas3BookingStatus.CONFIRMED,
            premises = buildCas3PremisesSummary(
              postcode = "SW1A 1A4",
            ),
          ),
          prisoner = buildPrisoner(prisonNumber = prisonNumber, inOutStatus = InOutStatus.OUT, prisonName = "A Prison"),
        ),
        upstreamFailures = emptyList(),
      )

      val result = accommodationQueryService.getNextAccommodation(crn)
      assertThat(result.data!!.address.postcode).isEqualTo("SW1A 1AB")
      assertThat(result.data!!.status!!.code).isEqualTo("PR")
    }

    @Test
    fun `getNextAccommodation should orchestrate calls and get no next accommodations when none are proposed`() {
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      every { caseRepository.findByCrn(crn) } returns caseEntity
      every { accommodationOrchestrationService.getNextAccommodationOrchestration(crn, prisonNumber) } returns OrchestrationResultDto(
        data = buildAccommodationOrchestrationDto(
          cpr = buildCorePersonRecord(
            addresses = listOf(
              buildCanonicalAddress(
                cprAddressId = UUID.randomUUID(),
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
              buildCanonicalAddress(
                cprAddressId = UUID.randomUUID(),
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
          cas1Application = null,
          cas3Application = null,
        ),
        upstreamFailures = emptyList(),
      )

      val result = accommodationQueryService.getNextAccommodation(crn)
      assertThat(result.data).isNull()
    }

    @Test
    fun `getNextAccommodation should return null data and upstream failure when calls fail`() {
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      every { caseRepository.findByCrn(crn) } returns caseEntity
      every { accommodationOrchestrationService.getNextAccommodationOrchestration(crn, prisonNumber) } returns OrchestrationResultDto(
        data = buildAccommodationOrchestrationDto(
          cpr = null,
          cas1Application = null,
          cas3Application = null,
        ),
        upstreamFailures = listOf(
          buildUpstreamFailure(
            callKey = ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN,
          ),
          buildUpstreamFailure(
            callKey = ApiCallKeys.GET_CAS_1_APPLICATION,
          ),
          buildUpstreamFailure(
            callKey = ApiCallKeys.GET_CAS_3_APPLICATION,
          ),
        ),
      )

      // when
      val result = accommodationQueryService.getNextAccommodation(crn)

      // then
      assertThat(result.data).isNull()
      assertThat(result.upstreamFailures.first().endpoint).isEqualTo(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN)
      assertThat(result.upstreamFailures[1].endpoint).isEqualTo(ApiCallKeys.GET_CAS_1_APPLICATION)
      assertThat(result.upstreamFailures.last().endpoint).isEqualTo(ApiCallKeys.GET_CAS_3_APPLICATION)
    }
  }

  @Nested
  inner class GetNextAccommodations {

    @Test
    fun `getNextAccommodations get the next accommodations`() {
      val addresses = listOf(
        buildCanonicalAddress(
          cprAddressId = UUID.randomUUID(),
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
        buildCanonicalAddress(
          cprAddressId = UUID.randomUUID(),
          noFixedAbode = false,
          postcode = "GL53 8GH",
          thoroughfareName = "",
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
      )
      val cas1Application = buildCas1Application(
        placementStatus = Cas1PlacementStatus.UPCOMING,
        premises = buildCas1PremisesSummary(
          postcode = "SW1A 1AB",
        ),
      )
      val cas3Application = buildCas3Application(
        bookingStatus = Cas3BookingStatus.CONFIRMED,
        premises = buildCas3PremisesSummary(
          postcode = "SW1A 1A4",
        ),
      )

      val result = accommodationQueryService.getNextAccommodations(crn, addresses, cas1Application, cas3Application, null)
      assertThat(result.size).isEqualTo(3)
      assertThat(result.first().address.postcode).isEqualTo("GL53 8GH")
      assertThat(result.first().status!!.code).isEqualTo("PR")

      assertThat(result[1].address.postcode).isEqualTo("SW1A 1AB")
      assertThat(result[1].status!!.code).isEqualTo("PR")

      assertThat(result.last().address.postcode).isEqualTo("SW1A 1A4")
      assertThat(result.last().status!!.code).isEqualTo("PR")
    }

    @Test
    fun `getNextAccommodations get the next accommodations without cas1`() {
      val addresses = listOf(
        buildCanonicalAddress(
          cprAddressId = UUID.randomUUID(),
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
        buildCanonicalAddress(
          cprAddressId = UUID.randomUUID(),
          noFixedAbode = false,
          postcode = "GL53 8GH",
          thoroughfareName = "",
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
      )
      val cas1Application = buildCas1Application(
        placementStatus = Cas1PlacementStatus.ARRIVED,
        premises = buildCas1PremisesSummary(
          postcode = "SW1A 1AB",
        ),
      )
      val cas3Application = buildCas3Application(
        bookingStatus = Cas3BookingStatus.CONFIRMED,
        premises = buildCas3PremisesSummary(
          postcode = "SW1A 1A4",
        ),
      )

      val result = accommodationQueryService.getNextAccommodations(crn, addresses, cas1Application, cas3Application, null)
      assertThat(result.size).isEqualTo(2)
      assertThat(result.first().address.postcode).isEqualTo("GL53 8GH")
      assertThat(result.first().status!!.code).isEqualTo("PR")

      assertThat(result.last().address.postcode).isEqualTo("SW1A 1A4")
      assertThat(result.last().status!!.code).isEqualTo("PR")
    }

    @Test
    fun `getNextAccommodations get the next accommodations without cas3`() {
      val addresses = listOf(
        buildCanonicalAddress(
          cprAddressId = UUID.randomUUID(),
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
        buildCanonicalAddress(
          cprAddressId = UUID.randomUUID(),
          noFixedAbode = false,
          postcode = "GL53 8GH",
          thoroughfareName = "",
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
      )
      val cas1Application = buildCas1Application(
        placementStatus = Cas1PlacementStatus.UPCOMING,
        premises = buildCas1PremisesSummary(
          postcode = "SW1A 1AB",
        ),
      )
      val cas3Application = buildCas3Application(
        bookingStatus = Cas3BookingStatus.ARRIVED,
        premises = buildCas3PremisesSummary(
          postcode = "SW1A 1A4",
        ),
      )

      val result = accommodationQueryService.getNextAccommodations(crn, addresses, cas1Application, cas3Application, null)
      assertThat(result.size).isEqualTo(2)
      assertThat(result.first().address.postcode).isEqualTo("GL53 8GH")
      assertThat(result.first().status!!.code).isEqualTo("PR")

      assertThat(result[1].address.postcode).isEqualTo("SW1A 1AB")
      assertThat(result[1].status!!.code).isEqualTo("PR")
    }
  }

  @Nested
  inner class GetProposedAccommodationById {
    private val id = UUID.randomUUID()

    @Test
    fun `should return accommodation when found by id`() {
      val createdByUserId = UUID.randomUUID()
      val accommodationTypeEntity = buildAccommodationTypeEntity()
      val accommodationStatusEntity = buildAccommodationStatusEntity()
      val proposedAccommodationEntity = buildProposedAccommodationEntity(
        id = id,
        cprAddressId = UUID.randomUUID(),
        typeVerified = true,
        noFixedAbode = true,
        caseId = caseId,
        accommodationTypeEntity = accommodationTypeEntity,
        accommodationStatusEntity = accommodationStatusEntity,
        createdByUserId = createdByUserId,
      )
      val case = buildCaseEntity(id = caseId)

      every { proposedAccommodationRepository.findByIdOrNull(id) } returns proposedAccommodationEntity
      every { caseRepository.findWithIdentifiersById(caseId) } returns case
      every { accommodationTypeRepository.findByIdOrNull(accommodationTypeEntity.id) } returns accommodationTypeEntity
      every { accommodationStatusRepository.findByIdOrNull(accommodationStatusEntity.id) } returns accommodationStatusEntity

      val result = accommodationQueryService.getAccommodation(id)

      assertThat(result.crn).isEqualTo(case.latestCrn())
      assertThat(result.cprAddressId).isEqualTo(proposedAccommodationEntity.cprAddressId)
      assertThat(result.typeVerified).isEqualTo(proposedAccommodationEntity.typeVerified)
      assertThat(result.noFixedAbode).isEqualTo(proposedAccommodationEntity.noFixedAbode)
      assertThat(result.startDate).isEqualTo(proposedAccommodationEntity.createdAt!!.atZone(ZoneId.systemDefault()).toLocalDate())
      assertThat(result.endDate).isNull()
      assertThat(result.address.postcode).isEqualTo(proposedAccommodationEntity.postcode)
      assertThat(result.status!!.code).isEqualTo(accommodationStatusEntity.code)
      assertThat(result.type!!.code).isEqualTo(accommodationTypeEntity.code)
    }

    @Test
    fun `should return accommodation with null status when proposed accommodation has no accommodationStatusId`() {
      val accommodationTypeEntity = buildAccommodationTypeEntity()
      val proposedAccommodationEntity = buildProposedAccommodationEntity(
        id = id,
        cprAddressId = UUID.randomUUID(),
        typeVerified = true,
        noFixedAbode = true,
        caseId = caseId,
        accommodationTypeEntity = accommodationTypeEntity,
        accommodationStatusEntity = null,
      )
      val case = buildCaseEntity(id = caseId)

      every { proposedAccommodationRepository.findByIdOrNull(id) } returns proposedAccommodationEntity
      every { caseRepository.findWithIdentifiersById(caseId) } returns case
      every { accommodationTypeRepository.findByIdOrNull(accommodationTypeEntity.id) } returns accommodationTypeEntity

      val result = accommodationQueryService.getAccommodation(id)

      assertThat(result.crn).isEqualTo(case.latestCrn())
      assertThat(result.cprAddressId).isEqualTo(proposedAccommodationEntity.cprAddressId)
      assertThat(result.typeVerified).isEqualTo(proposedAccommodationEntity.typeVerified)
      assertThat(result.noFixedAbode).isEqualTo(proposedAccommodationEntity.noFixedAbode)
      assertThat(result.startDate).isEqualTo(proposedAccommodationEntity.createdAt!!.atZone(ZoneId.systemDefault()).toLocalDate())
      assertThat(result.endDate).isNull()
      assertThat(result.address.postcode).isEqualTo(proposedAccommodationEntity.postcode)
      assertThat(result.status).isNull()
      assertThat(result.type!!.code).isEqualTo(accommodationTypeEntity.code)

      verify(exactly = 0) {
        accommodationStatusRepository.findByIdOrNull(any())
      }
    }

    @Test
    fun `should throw NotFoundException when not found`() {
      every { proposedAccommodationRepository.findByIdOrNull(id) } returns null

      assertThatThrownBy { accommodationQueryService.getAccommodation(id) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("ProposedAccommodationEntity not found for [id=$id]")
    }

    @Test
    fun `should throw NotFoundException when case not found`() {
      val accommodationTypeEntity = buildAccommodationTypeEntity()
      val accommodationStatusEntity = buildAccommodationStatusEntity()
      val proposedAccommodationEntity = buildProposedAccommodationEntity(
        id = id,
        cprAddressId = UUID.randomUUID(),
        caseId = caseId,
        accommodationTypeEntity = accommodationTypeEntity,
        accommodationStatusEntity = accommodationStatusEntity,
      )

      every { proposedAccommodationRepository.findByIdOrNull(id) } returns proposedAccommodationEntity
      every { caseRepository.findWithIdentifiersById(caseId) } returns null

      assertThatThrownBy { accommodationQueryService.getAccommodation(id) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("CaseEntity not found for [id=${proposedAccommodationEntity.id}]")
    }

    @Test
    fun `should throw NotFoundException when accommodation type not found`() {
      val accommodationTypeEntity = buildAccommodationTypeEntity()
      val accommodationStatusEntity = buildAccommodationStatusEntity()
      val proposedAccommodationEntity = buildProposedAccommodationEntity(
        id = id,
        cprAddressId = UUID.randomUUID(),
        caseId = caseId,
        accommodationTypeEntity = accommodationTypeEntity,
        accommodationStatusEntity = accommodationStatusEntity,
      )
      val case = buildCaseEntity(id = caseId)

      every { proposedAccommodationRepository.findByIdOrNull(id) } returns proposedAccommodationEntity
      every { caseRepository.findWithIdentifiersById(caseId) } returns case
      every { accommodationTypeRepository.findByIdOrNull(accommodationTypeEntity.id) } returns null

      assertThatThrownBy { accommodationQueryService.getAccommodation(id) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("AccommodationTypeEntity not found for [id=${proposedAccommodationEntity.accommodationTypeId}]")
    }

    @Test
    fun `should throw NotFoundException when accommodation status not found`() {
      val accommodationTypeEntity = buildAccommodationTypeEntity()
      val accommodationStatusEntity = buildAccommodationStatusEntity()
      val proposedAccommodationEntity = buildProposedAccommodationEntity(
        id = id,
        cprAddressId = UUID.randomUUID(),
        caseId = caseId,
        accommodationTypeEntity = accommodationTypeEntity,
        accommodationStatusEntity = accommodationStatusEntity,
      )
      val case = buildCaseEntity(id = caseId)

      every { proposedAccommodationRepository.findByIdOrNull(id) } returns proposedAccommodationEntity
      every { caseRepository.findWithIdentifiersById(caseId) } returns case
      every { accommodationTypeRepository.findByIdOrNull(accommodationTypeEntity.id) } returns accommodationTypeEntity
      every { accommodationStatusRepository.findByIdOrNull(accommodationStatusEntity.id) } returns null

      assertThatThrownBy { accommodationQueryService.getAccommodation(id) }
        .isInstanceOf(NotFoundException::class.java)
        .hasMessage("AccommodationStatusEntity not found for [id=${proposedAccommodationEntity.accommodationStatusId}]")
    }
  }
}
