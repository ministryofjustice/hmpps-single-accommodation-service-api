package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.accommodation

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.accommodation.AccommodationSummaryCalculator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import java.time.LocalDate
import java.util.UUID
import java.util.stream.Stream

@ExtendWith(MockKExtension::class)
class AccommodationSummaryCalculatorCsvTest {

  @MockK
  lateinit var accommodationTypeRepository: AccommodationTypeRepository

  @MockK
  @Suppress("unused")
  lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  @InjectMockKs
  lateinit var calculator: AccommodationSummaryCalculator

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

  @BeforeEach
  fun setup() {
    every { accommodationTypeRepository.findAllBySettledTypeAndActiveIsTrue(AccommodationSettledType.TRANSIENT) } returns
      listOf(buildAccommodationTypeEntity(code = "A03", settledType = AccommodationSettledType.TRANSIENT))
    every { accommodationTypeRepository.findAllBySettledTypeAndActiveIsTrue(AccommodationSettledType.SETTLED) } returns
      listOf(buildAccommodationTypeEntity(code = "A01A", settledType = AccommodationSettledType.SETTLED))
    every { accommodationTypeRepository.findAllByIsHomelessIsTrueAndActiveIsTrue() } returns
      listOf(buildAccommodationTypeEntity(code = "A08", isHomeless = true))
    every { accommodationTypeRepository.findByCode(any()) } returns null
  }

  @Nested
  inner class NoFixedAbode {
    @ParameterizedTest(name = "{0}")
    @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.accommodation.AccommodationSummaryCalculatorCsvTest#noFixedAbodeScenarios")
    fun `returns no fixed abode`(scenario: CaseAccommodationScenario) {
      assertCaseAccommodationStatus(scenario, CaseAccommodationStatus.NO_FIXED_ABODE)
    }
  }

  @Nested
  inner class RiskOfNoFixedAbode {
    @ParameterizedTest(name = "{0}")
    @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.accommodation.AccommodationSummaryCalculatorCsvTest#riskOfNoFixedAbodeScenarios")
    fun `returns risk of no fixed abode`(scenario: CaseAccommodationScenario) {
      assertCaseAccommodationStatus(scenario, CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE)
    }
  }

  @Nested
  inner class Settled {
    @ParameterizedTest(name = "{0}")
    @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.accommodation.AccommodationSummaryCalculatorCsvTest#settledScenarios")
    fun `returns settled`(scenario: CaseAccommodationScenario) {
      assertCaseAccommodationStatus(scenario, CaseAccommodationStatus.SETTLED)
    }
  }

  @Nested
  inner class Transient {
    @ParameterizedTest(name = "{0}")
    @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.accommodation.AccommodationSummaryCalculatorCsvTest#transientScenarios")
    fun `returns transient`(scenario: CaseAccommodationScenario) {
      assertCaseAccommodationStatus(scenario, CaseAccommodationStatus.TRANSIENT)
    }
  }

  @Nested
  inner class NullStatus {
    @ParameterizedTest(name = "{0}")
    @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.accommodation.AccommodationSummaryCalculatorCsvTest#nullStatusScenarios")
    fun `returns null`(scenario: CaseAccommodationScenario) {
      assertCaseAccommodationStatus(scenario, null)
    }
  }

  private fun assertCaseAccommodationStatus(
    scenario: CaseAccommodationScenario,
    expectedStatus: CaseAccommodationStatus?,
  ) {
    val result = calculator.calculateCaseAccommodationStatus(
      currentAccommodation = buildAccommodation(scenario.currentType, scenario.currentEndDate),
      nextAccommodation = buildAccommodation(scenario.nextType, scenario.nextEndDate),
    )

    assertThat(result)
      .describedAs("Scenario %s (%s)", scenario.id, scenario.testName)
      .isEqualTo(expectedStatus)
  }

  private fun buildAccommodation(typeCode: String?, endDate: String?): AccommodationSummaryDto? {
    val code = typeCode.nullIfBlank()?.toAccommodationTypeCode() ?: return null

    return buildAccommodationSummaryDto(
      type = buildAccommodationTypeDto(code = code),
      endDate = endDate.nullIfBlank()?.let(LocalDate::parse),
    )
  }

  companion object {
    private const val csvResourcePath = "/accommodation/CaseAccommodationStatusScenarios.csv"

    data class CaseAccommodationScenario(
      val id: Int,
      val testName: String,
      val currentType: String?,
      val currentEndDate: String?,
      val nextType: String?,
      val nextEndDate: String?,
      val expectedStatus: CaseAccommodationStatus?,
    ) {
      override fun toString(): String = "[$id] ${toDisplayLabel(currentType, currentEndDate)} -> ${toDisplayLabel(nextType, nextEndDate)}"
    }

    private val allScenarios: List<CaseAccommodationScenario> by lazy {
      loadScenarios()
    }

    @JvmStatic
    fun noFixedAbodeScenarios(): Stream<Arguments> = scenariosFor(CaseAccommodationStatus.NO_FIXED_ABODE)

    @JvmStatic
    fun riskOfNoFixedAbodeScenarios(): Stream<Arguments> = scenariosFor(CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE)

    @JvmStatic
    fun settledScenarios(): Stream<Arguments> = scenariosFor(CaseAccommodationStatus.SETTLED)

    @JvmStatic
    fun transientScenarios(): Stream<Arguments> = scenariosFor(CaseAccommodationStatus.TRANSIENT)

    @JvmStatic
    fun nullStatusScenarios(): Stream<Arguments> = allScenarios
      .filter { it.expectedStatus == null }
      .map { Arguments.of(it) }
      .stream()

    private fun scenariosFor(expectedStatus: CaseAccommodationStatus): Stream<Arguments> = allScenarios
      .filter { it.expectedStatus == expectedStatus }
      .map { Arguments.of(it) }
      .stream()

    private fun loadScenarios(): List<CaseAccommodationScenario> {
      val lines = AccommodationSummaryCalculatorCsvTest::class.java
        .getResourceAsStream(csvResourcePath)
        ?.bufferedReader()
        ?.use { it.readLines() }
        ?: error("CSV not found on classpath: $csvResourcePath")

      require(lines.isNotEmpty()) { "CSV is empty: $csvResourcePath" }

      val headers = splitCsvLine(lines.first(), expectedColumns = null)

      return lines
        .drop(1)
        .filter { it.isNotBlank() }
        .mapIndexed { rowIndex, line ->
          val values = splitCsvLine(line, expectedColumns = headers.size)
          val row = headers.zip(values).toMap()

          try {
            CaseAccommodationScenario(
              id = row.getValue("id").toInt(),
              testName = row.getValue("testName"),
              currentType = row.getValue("currentType").nullIfBlank(),
              currentEndDate = row.getValue("currentEndDate").nullIfBlank(),
              nextType = row.getValue("nextType").nullIfBlank(),
              nextEndDate = row.getValue("nextEndDate").nullIfBlank(),
              expectedStatus = row.getValue("expectedStatus").toCaseAccommodationStatusOrNull(),
            )
          } catch (e: Exception) {
            throw IllegalStateException("Failed to parse CSV row ${rowIndex + 2} from $csvResourcePath: $row", e)
          }
        }
    }

    private fun splitCsvLine(line: String, expectedColumns: Int?): List<String> {
      val columns = line.split(',', ignoreCase = false, limit = expectedColumns ?: 0)

      if (expectedColumns != null && columns.size != expectedColumns) {
        error("Expected $expectedColumns columns but found ${columns.size} in line: $line")
      }

      return columns
    }

    private fun String?.nullIfBlank(): String? = this?.takeIf { it.isNotBlank() }

    private fun String.toCaseAccommodationStatusOrNull(): CaseAccommodationStatus? = takeUnless { it == "NULL" }?.let(CaseAccommodationStatus::valueOf)

    private fun String.toAccommodationTypeCode(): String = when (this) {
      "HOMELESS" -> "A08"
      "SETTLED" -> "A01A"
      "TRANSIENT" -> "A03"
      "UNKNOWN" -> "UNKNOWN_CODE"
      else -> error("Unknown accommodation type name in CSV: $this")
    }

    private fun toDisplayLabel(type: String?, endDate: String?): String = when (type) {
      null -> "null"
      "HOMELESS" -> if (endDate == null) "homeless" else "homeless with end"
      "SETTLED" -> if (endDate == null) "settled" else "settled with end"
      "TRANSIENT" -> if (endDate == null) "transient" else "transient with end"
      "UNKNOWN" -> if (endDate == null) "unknown" else "unknown with end"
      else -> error("Unknown accommodation type name in CSV: $type")
    }
  }
}
