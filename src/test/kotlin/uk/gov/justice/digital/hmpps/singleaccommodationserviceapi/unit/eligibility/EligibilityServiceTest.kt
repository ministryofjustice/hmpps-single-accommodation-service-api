package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.orchestration.EligibilityOrchestrationService
import java.time.OffsetDateTime
import java.util.UUID
import java.util.stream.Stream

@ExtendWith(MockKExtension::class)
class EligibilityServiceTest {
  @MockK
  lateinit var eligibilityOrchestrationService: EligibilityOrchestrationService

  @InjectMockKs
  lateinit var eligibilityService: EligibilityService

  private val crn = "ABC1234"

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.EligibilityServiceTest#provideDataAndEligibility")
  fun `should transform from case orchestration dto to case dto`(
    data: DomainData,
    expectedEligibilityDto: EligibilityDto,
  ) {
    every { eligibilityOrchestrationService.getData(any()) } returns data
    val result = eligibilityService.getEligibility(crn)
    assertThat(
      result,
    ).isEqualTo(expectedEligibilityDto)
  }

  private companion object {

    private const val CRN = "ABC1234"
    private const val CREATE_PLACEMENT_ACTION = "Create a placement request."

    private val male = Sex(
      code = "M",
      description = "Male",
    )

    @JvmStatic
    fun provideDataAndEligibility(): Stream<Arguments> = Stream.of(
      Arguments.of(
        DomainData(
          crn = CRN,
          tier = "A1",
          sex = male,
          releaseDate = OffsetDateTime.now(),
          cas1Application = Cas1Application(
            id = UUID.randomUUID(),
            applicationStatus = Cas1ApplicationStatus.PlacementAllocated,
            placementStatus = Cas1PlacementStatus.DEPARTED,
          ),
        ),
        EligibilityDto(
          crn = CRN,
          cas1 = ServiceResult(
            serviceStatus = ServiceStatus.DEPARTED,
            actions = listOf(CREATE_PLACEMENT_ACTION),
          ),
        ),
      ),
      Arguments.of(
        DomainData(
          crn = CRN,
          tier = "A1",
          sex = male,
          releaseDate = OffsetDateTime.now(),
          cas1Application = Cas1Application(
            id = UUID.randomUUID(),
            applicationStatus = Cas1ApplicationStatus.AssesmentInProgress,
            placementStatus = Cas1PlacementStatus.NOT_ALLOCATED,
          ),
        ),
        EligibilityDto(
          crn = CRN,
          cas1 = ServiceResult(
            serviceStatus = ServiceStatus.ASSESSMENT_IN_PROGRESS,
            actions = listOf(CREATE_PLACEMENT_ACTION),
          ),
        ),
      ),
      Arguments.of(
        DomainData(
          crn = CRN,
          tier = "A1",
          sex = male,
          releaseDate = OffsetDateTime.now(),
          cas1Application = Cas1Application(
            id = UUID.randomUUID(),
            applicationStatus = Cas1ApplicationStatus.PlacementAllocated,
            placementStatus = Cas1PlacementStatus.UPCOMING,
          ),
        ),
        EligibilityDto(
          crn = CRN,
          cas1 = ServiceResult(
            serviceStatus = ServiceStatus.UPCOMING_PLACEMENT,
            actions = listOf(),
          ),
        ),
      ),
      Arguments.of(
        DomainData(
          crn = CRN,
          tier = "A1",
          sex = male,
          releaseDate = OffsetDateTime.now().plusMonths(7),
        ),
        EligibilityDto(
          crn = CRN,
          cas1 = ServiceResult(
            serviceStatus = ServiceStatus.UPCOMING,
            actions = listOf("Start approved premise referral in 31 days"),
          ),
        ),
      ),
      Arguments.of(
        DomainData(
          crn = CRN,
          tier = "A1",
          sex = male,
          releaseDate = OffsetDateTime.now(),
        ),
        EligibilityDto(
          crn = CRN,
          cas1 = ServiceResult(
            serviceStatus = ServiceStatus.NOT_STARTED,
            actions = listOf("Start approved premise referral"),
          ),
        ),
      ),
      Arguments.of(
        DomainData(
          crn = CRN,
          tier = "A1S",
          sex = male,
          releaseDate = OffsetDateTime.now(),
        ),
        EligibilityDto(
          crn = CRN,
          cas1 = ServiceResult(
            serviceStatus = ServiceStatus.NOT_ELIGIBLE,
            actions = listOf(),
          ),
        ),
      ),
    )
  }
}
