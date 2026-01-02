package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.toFullName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factory.buildCaseOrchestrationDto
import java.time.LocalDate
import java.util.stream.Stream

class CaseTransformerTest {

  @ParameterizedTest
  @MethodSource(
    "uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.case.CaseTransformerTest#caseTransformationCases",
  )
  fun `should transform from case orchestration dto to case dto`(
    caseOrchestrationDto: CaseOrchestrationDto,
    expectedCaseDto: CaseDto,
  ) {
      assertThat(
        toCaseDto(
          crn = caseOrchestrationDto.crn,
          cpr = caseOrchestrationDto.cpr,
          roshDetails = caseOrchestrationDto.roshDetails,
          tier = caseOrchestrationDto.tier,
          caseSummaries = caseOrchestrationDto.cases,
        )
      ).isEqualTo(expectedCaseDto)
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "John,Paul Andrew, Smith, John Paul Andrew Smith",
      "John, null, Smith, John Smith",
      "John, '', Smith, John Smith",
      "John, '       ', Smith, John Smith",
      "Firstname, MiddleName, null, Firstname MiddleName",
      "null, MiddleName, Lastname, MiddleName Lastname",
      "Alice, X, Wonderland, Alice X Wonderland",
      "Alice, X Y Z 1, Wonder land, Alice X Y Z 1 Wonder land",
      "null, null, null, ''",
    ],
    nullValues = ["null"],
  )
  fun `should format full-name correctly`(
    firstname: String?,
    middleNames: String?,
    lastName: String?,
    expected: String,
  ) {
    val cpr = buildCorePersonRecord(firstName = firstname, middleNames = middleNames, lastName = lastName)
    assertThat(toFullName(cpr)).isEqualTo(expected)
  }

  private companion object {
    private const val CRN = "X12345"

    private val caseDtoWhenAllDataSupplied = CaseDto(
      name = "First Middle Last",
      dateOfBirth = LocalDate.of(2000, 12, 3),
      crn = CRN,
      prisonNumber = "PRI1",
      tier = TierScore.C1,
      riskLevel = RiskLevel.VERY_HIGH,
      pncReference = "Some PNC Reference",
      assignedTo = AssignedToDto(1L, name = "Team 1"),
      photoUrl = null,
      currentAccommodation = null,
      nextAccommodation = null
    )

    @JvmStatic
    fun caseTransformationCases(): Stream<Arguments> {
      val caseWithAllData = buildCaseOrchestrationDto(CRN)
      val caseWithoutCaseSummary = caseWithAllData.copy(
        cases = emptyList(),
      )
      val caseWithCprWithNoIdentifiers = caseWithAllData.copy(
        cpr = buildCorePersonRecord(identifiers = null),
      )
      val caseWithCprWithEmptyPrisonNumberAndPncsIdentifiers = caseWithAllData.copy(
        cpr = buildCorePersonRecord(
          identifiers = buildIdentifiers(
            prisonNumbers = emptyList(),
            pncs = emptyList(),
          ),
        ),
      )

      val expectedCaseWithoutAssignedToData = caseDtoWhenAllDataSupplied.copy(
        assignedTo = null,
      )
      val expectedCaseWithoutPrisonNumberData = caseDtoWhenAllDataSupplied.copy(
        prisonNumber = null,
        pncReference = null,
      )

      return Stream.of(
        Arguments.of(caseWithAllData, caseDtoWhenAllDataSupplied),
        Arguments.of(caseWithoutCaseSummary, expectedCaseWithoutAssignedToData),
        Arguments.of(caseWithCprWithNoIdentifiers, expectedCaseWithoutPrisonNumberData),
        Arguments.of(caseWithCprWithEmptyPrisonNumberAndPncsIdentifiers, expectedCaseWithoutPrisonNumberData),
      )
    }
  }
}
