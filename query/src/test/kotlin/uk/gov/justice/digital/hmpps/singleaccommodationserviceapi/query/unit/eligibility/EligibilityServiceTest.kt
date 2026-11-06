package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailureTransformer.toUpstreamFailureDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DeeplinkResolver
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.Cas1EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.Cas2EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.Cas3EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.CrsEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.DtrEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.PaEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataFactory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.EligibilityOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.EligibilityOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.output.EligibilityNewToOldTransformer.toEligibilityDtoOld
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildEligibilityDtoNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildUpstreamFailure

@ExtendWith(MockKExtension::class)
class EligibilityServiceTest {

  @MockK
  private lateinit var domainDataFactory: DomainDataFactory

  @MockK
  private lateinit var caseRepository: CaseRepository

  @MockK
  private lateinit var eligibilityOrchestrationService: EligibilityOrchestrationService

  @MockK
  private lateinit var deeplinkResolver: DeeplinkResolver

  @MockK
  private lateinit var cas1Tree: Cas1EligibilityTreeProvider

  @MockK
  private lateinit var cas2Tree: Cas2EligibilityTreeProvider

  @MockK
  private lateinit var cas3Tree: Cas3EligibilityTreeProvider

  @MockK
  private lateinit var dtrTree: DtrEligibilityTreeProvider

  @MockK
  private lateinit var crsTree: CrsEligibilityTreeProvider

  @MockK
  private lateinit var paTree: PaEligibilityTreeProvider

  @InjectMockKs
  private lateinit var eligibilityService: EligibilityService

  @Nested
  inner class GetEligibility {
    val crn = "ABC123"

    @Test
    fun `getEligibility returns failed eligibility and upstream failures when external api fails`() {
      val upstreamFailure = buildUpstreamFailure()
      val orchestrationDto = OrchestrationResultDto(
        data = EligibilityOrchestrationDto(
          crn = crn,
          cpr = null,
          tier = null,
          cas1Application = null,
          cas2Application = null,
          cas3Application = null,
          commissionedRehabilitativeServices = null,
          prisoner = null,
          cas1CurrentPremises = null,
          cas3CurrentPremises = null,
          sexualOffenceRegistrations = null,
          case = null,
        ),
        upstreamFailures = listOf(
          upstreamFailure,
        ),
      )

      every { caseRepository.findByCrn(crn) } returns null
      every { eligibilityOrchestrationService.getData(crn, null) } returns orchestrationDto

      val result = eligibilityService.getEligibility(crn)

      val eligibility = buildEligibilityDtoNew(crn)

      val expected = ApiResponseDto(
        data = toEligibilityDtoOld(eligibility),
        upstreamFailures = listOf(toUpstreamFailureDto(upstreamFailure)),
      )
      assertThat(result).isEqualTo(expected)
    }
  }
}
