package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.AssertionsForClassTypes.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.accommodation.AccommodationSummaryCalculator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1UrlTemplates
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2UrlTemplates
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3UrlTemplates
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.RulesConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DeeplinkResolver
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataFactory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.EligibilityOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate
import java.time.format.DateTimeFormatter

const val CAS1_APPLICATION_START_URL = "CAS1_APPLICATION_START_URL"
private const val CAS2_APPLICATION_START_URL = "CAS2_APPLICATION_START_URL#crn"
const val CAS3_REFERRAL_START_URL = "CAS3_REFERRAL_START_URL"
const val CRS_UI_URL = "CRS_UI_URL"

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [EligibilityScenarioTestBase.TestConfig::class])
@TestPropertySource(properties = ["service.commissioned-rehabilitative-services-ui.base-url=$CRS_UI_URL"])
abstract class EligibilityScenarioTestBase {

  @Configuration
  @ComponentScan(basePackageClasses = [DecisionTreeBuilder::class, DeeplinkResolver::class])
  @Import(RulesConfig::class, EligibilityService::class, DomainDataFactory::class)
  class TestConfig {
    @Bean
    fun clock(): MutableClock = MutableClock()

    @Bean
    fun sentryService(): SentryService = mockk(relaxed = true)

    @Bean
    fun approvedPremisesCachingService(): ApprovedPremisesCachingService = mockk {
      every { getCas1UrlTemplates() } returns Cas1UrlTemplates(CAS1_APPLICATION_START_URL)
      every { getCas2UrlTemplates() } returns Cas2UrlTemplates(CAS2_APPLICATION_START_URL)
      every { getCas3UrlTemplates() } returns Cas3UrlTemplates(CAS3_REFERRAL_START_URL)
    }

    @Bean
    fun accommodationSummaryCalculator(): AccommodationSummaryCalculator = mockk()

    @Bean
    fun accommodationTypeRepository(): AccommodationTypeRepository = mockk()

    @Bean
    fun caseRepository(): CaseRepository = mockk()

    @Bean
    fun dutyToReferQueryService(): DutyToReferQueryService = mockk()

    @Bean
    fun eligibilityOrchestrationService(): EligibilityOrchestrationService = mockk()
  }

  @Autowired
  protected lateinit var clock: MutableClock

  @Autowired
  protected lateinit var eligibilityService: EligibilityService

  @BeforeEach
  fun resetState() {
    clock.reset()
  }

  private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  protected fun String.toLocalDate(): LocalDate = LocalDate.parse(this, dateFormatter)

  protected fun String?.expandCurrentAccommodationType(): List<String?> = when (this) {
    "A10-A11" -> listOf("A10", "A11")
    "notPrisonCas1OrCas2" -> "notPrisonOrCas1".expandCurrentAccommodationType()
      .filterNot { it == "A10" || it == "A11" }

    "notPrisonOrCas1" -> listOf(
      "A16",
      "A10",
      "A11",
      "A17",
      "A07B",
      "A07A",
      "A14",
      "A13",
      "A08A",
      "A08C",
      "A08",
      "A01A",
      "A15",
      "A12",
      "A01C",
      "A01D",
      "A04",
      "A03",
    )

    else -> listOf(this)
  }

  protected fun <T> runScenarios(
    scenarios: List<T>,
    run: (T) -> Unit,
  ) {
    val failures = mutableListOf<String>()

    scenarios.forEach { scenario ->
      try {
        run(scenario)
      } catch (ex: AssertionError) {
        failures += """
        Scenario failed:
        scenario: $scenario
        error: ${ex.message}
        """.trimIndent()
      } catch (ex: Exception) {
        failures += """
        Scenario errored:
        scenario: $scenario
        error: ${ex::class.simpleName}: ${ex.message}
        """.trimIndent()
      }
    }

    if (failures.isNotEmpty()) {
      fail(
        """
      Scenario failures: ${failures.size}

      ${failures.joinToString("\n\n")}
        """.trimIndent(),
      )
    }
  }
}
