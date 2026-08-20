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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummariesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.accommodation.AccommodationSummaryCalculator
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PlacementSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
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
import java.util.UUID

@ExtendWith(MockKExtension::class)
class AccommodationQueryServiceTest {
  @MockK
  lateinit var accommodationOrchestrationService: AccommodationOrchestrationService

  @MockK
  lateinit var accommodationSummaryCalculator: AccommodationSummaryCalculator

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
                usages = listOf(
                  CanonicalAddressUsage(
                    usageCode = CanonicalAddressUsageCode(
                      code = AddressUsageCode.A07B.name,
                      description = AddressUsageCode.A07B.description,
                    ),
                    isActive = true,
                  ),
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
                usages = listOf(
                  CanonicalAddressUsage(
                    usageCode = CanonicalAddressUsageCode(
                      code = AddressUsageCode.A07A.name,
                      description = AddressUsageCode.A07A.description,
                    ),
                    isActive = true,
                  ),
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
                usages = listOf(
                  CanonicalAddressUsage(
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
                usages = listOf(
                  CanonicalAddressUsage(
                    usageCode = CanonicalAddressUsageCode(
                      code = AddressUsageCode.A07B.name,
                      description = AddressUsageCode.A07B.description,
                    ),
                    isActive = true,
                  ),
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
                usages = listOf(
                  CanonicalAddressUsage(
                    usageCode = CanonicalAddressUsageCode(
                      code = AddressUsageCode.A07A.name,
                      description = AddressUsageCode.A07A.description,
                    ),
                    isActive = true,
                  ),
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
                usages = listOf(
                  CanonicalAddressUsage(
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
    fun `getAccommodationHistory should orchestrate calls and map addresses and sort by start date`() {
      val referenceDate = LocalDate.of(2024, 10, 17)
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
                startDate = referenceDate.minusDays(3),
                cprAddressId = null,
                noFixedAbode = false,
                postcode = "GL53 8GH",
                thoroughfareName = "Another Road",
                postTown = "Cheltenham",
                status = CanonicalAddressStatus(
                  code = AddressStatusCode.P.name,
                  description = AddressStatusCode.P.description,
                ),
                usages = listOf(
                  CanonicalAddressUsage(
                    usageCode = CanonicalAddressUsageCode(
                      code = AddressUsageCode.A07A.name,
                      description = AddressUsageCode.A07A.description,
                    ),
                    isActive = true,
                  ),
                ),
              ),
              buildCanonicalAddress(
                startDate = referenceDate.minusDays(2),
                cprAddressId = UUID.randomUUID(),
                noFixedAbode = false,
                postcode = "GL53 8G4",
                thoroughfareName = null,
                postTown = null,
                endDate = LocalDate.of(2025, 10, 17),
                status = CanonicalAddressStatus(
                  code = AddressStatusCode.P.name,
                  description = AddressStatusCode.P.description,
                ),
                usages = listOf(
                  CanonicalAddressUsage(
                    usageCode = CanonicalAddressUsageCode(
                      code = AddressUsageCode.A08A.name,
                      description = AddressUsageCode.A08A.description,
                    ),
                    isActive = true,
                  ),
                ),
              ),
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
                startDate = referenceDate,
                usages = listOf(
                  CanonicalAddressUsage(
                    usageCode = CanonicalAddressUsageCode(
                      code = AddressUsageCode.A07B.name,
                      description = AddressUsageCode.A07B.description,
                    ),
                    isActive = true,
                  ),
                ),
              ),
            ),
          ),
        ),
        upstreamFailures = emptyList(),
      )

      val result = accommodationQueryService.getAccommodationHistory(crn)

      assertThat(result.data.size).isEqualTo(4)
      assertThat(result.data[0].address.buildingName).isEqualTo(prisoner.prisonName)
      assertThat(result.data[0].status!!.code).isEqualTo("C")
      assertThat(result.data[1].address.postcode).isEqualTo("SW1A 1AA")
      assertThat(result.data[1].status!!.code).isEqualTo(AddressStatusCode.M.name)
      assertThat(result.data[2].address.postcode).isEqualTo("GL53 8G4")
      assertThat(result.data[2].status!!.code).isEqualTo(AddressStatusCode.P.name)
      assertThat(result.data[3].address.postcode).isEqualTo("GL53 8GH")
      assertThat(result.data[3].status!!.code).isEqualTo(AddressStatusCode.P.name)
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
    fun `getCurrentAccommodation should orchestrate calls and get the current accommodation`() {
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      val addresses = listOf(
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
          usages = listOf(
            CanonicalAddressUsage(
              usageCode = CanonicalAddressUsageCode(
                code = AddressUsageCode.A01A.name,
                description = AddressUsageCode.A01A.description,
              ),
              isActive = true,
            ),
          ),
        ),
      )
      val expectedResult = buildAccommodationSummaryDto(
        crn = crn,
        address = buildAccommodationAddressDetails(postcode = "SW1A 1AA"),
        status = buildAccommodationStatusDto(code = "M", description = AddressStatusCode.M.description),
      )
      every { caseRepository.findByCrn(crn) } returns caseEntity
      every {
        accommodationOrchestrationService.getAccommodationOrchestration(
          crn,
          prisonNumber,
        )
      } returns OrchestrationResultDto(
        data = buildAccommodationOrchestrationDto(
          cpr = buildCorePersonRecord(addresses = addresses),
          prisoner = null,
        ),
        upstreamFailures = emptyList(),
      )
      every {
        accommodationSummaryCalculator.calculateCurrentAccommodation(
          crn = crn,
          addresses = addresses,
          prisoner = null,
          cas1CurrentPremises = null,
          cas3CurrentPremises = null,
        )
      } returns expectedResult

      val result = accommodationQueryService.getCurrentAccommodation(crn)

      assertThat(result.data).isEqualTo(expectedResult)
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
  inner class GetAllAccommodations {
    @Test
    fun `getAllAccommodations should orchestrate call and return all accommodations`() {
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      every { caseRepository.findByCrn(crn) } returns caseEntity
      every {
        accommodationOrchestrationService.getAccommodationOrchestration(
          crn,
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
                usages = listOf(
                  CanonicalAddressUsage(
                    usageCode = CanonicalAddressUsageCode(
                      code = AddressUsageCode.A01A.name,
                      description = AddressUsageCode.A01A.description,
                    ),
                    isActive = true,
                  ),
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
                usages = listOf(
                  CanonicalAddressUsage(
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
        ),
        upstreamFailures = emptyList(),
      )

      val result = accommodationQueryService.getAllAccommodations(crn)

      assertThat(result.data.size).isEqualTo(2)
      assertThat(result.data[0].address.postcode).isEqualTo("SW1A 1AA")
      assertThat(result.data[0].status!!.code).isEqualTo(AddressStatusCode.M.name)
      assertThat(result.data[1].address.postcode).isEqualTo("GL53 8GH")
      assertThat(result.data[1].status!!.code).isEqualTo(AddressStatusCode.P.name)
      assertThat(result.upstreamFailures.size).isEqualTo(0)
    }

    @Test
    fun `getAllAccommodations should return empty all accommodations when cpr is null`() {
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      every { caseRepository.findByCrn(crn) } returns caseEntity
      every {
        accommodationOrchestrationService.getAccommodationOrchestration(
          crn,
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

      val result = accommodationQueryService.getAllAccommodations(crn)

      assertThat(result.data).isEmpty()
      assertThat(result.upstreamFailures.first().endpoint).isEqualTo(ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN)
    }
  }

  @Nested
  inner class GetNextAccommodation {
    @Test
    fun `getNextAccommodation should orchestrate calls and get the next accommodation`() {
      val caseEntity = buildCaseEntity {
        withCrn(crn)
        withPrisonNumber(prisonNumber)
      }
      val cpr = buildCorePersonRecord(addresses = emptyList())
      val cas1Application = buildCas1Application(
        placement = buildCas1PlacementSummary(
          status = Cas1PlacementStatus.UPCOMING,
          premises = buildCas1PremisesSummary(),
        ),
      )
      val cas3Application = buildCas3Application(bookingStatus = Cas3BookingStatus.CONFIRMED)
      val prisoner = buildPrisoner(prisonNumber = prisonNumber, inOutStatus = InOutStatus.OUT)
      val expectedNextAccommodation = buildAccommodationSummaryDto(crn = crn, address = buildAccommodationAddressDetails(postcode = "SW1A 1AB"))
      every { caseRepository.findByCrn(crn) } returns caseEntity
      every { accommodationOrchestrationService.getNextAccommodationOrchestration(crn, prisonNumber) } returns OrchestrationResultDto(
        data = buildAccommodationOrchestrationDto(
          cpr = cpr,
          cas1Application = cas1Application,
          cas3Application = cas3Application,
          prisoner = prisoner,
        ),
        upstreamFailures = emptyList(),
      )
      every {
        accommodationSummaryCalculator.calculateAccommodationSummaries(
          crn = crn,
          addresses = cpr.addresses,
          prisoner = prisoner,
          cas1CurrentPremises = null,
          cas3CurrentPremises = null,
          cas1Application = cas1Application,
          cas3Application = cas3Application,
        )
      } returns AccommodationSummariesDto(
        currentAccommodation = null,
        nextAccommodation = expectedNextAccommodation,
      )

      val result = accommodationQueryService.getNextAccommodation(crn)

      assertThat(result.data).isEqualTo(expectedNextAccommodation)
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
          prisoner = null,
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
      every {
        accommodationSummaryCalculator.calculateAccommodationSummaries(
          crn = crn,
          addresses = null,
          prisoner = null,
          cas1CurrentPremises = null,
          cas3CurrentPremises = null,
          cas1Application = null,
          cas3Application = null,
        )
      } returns AccommodationSummariesDto(currentAccommodation = null, nextAccommodation = null)

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
        startDate = LocalDate.now().minusDays(1),
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
      assertThat(result.startDate).isEqualTo(proposedAccommodationEntity.startDate)
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
        startDate = LocalDate.now().minusDays(1),
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
      assertThat(result.startDate).isEqualTo(proposedAccommodationEntity.startDate)
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
