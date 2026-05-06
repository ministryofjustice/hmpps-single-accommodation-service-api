package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LAOStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonTransformer.toPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildEligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildFullPersonDto
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
      CaseTransformer.toCaseDto(
        cpr = caseOrchestrationDto.cpr,
        roshDetails = caseOrchestrationDto.roshDetails,
        tier = caseOrchestrationDto.tier,
        person = toPersonDto(caseOrchestrationDto.case!!),
      ),
    ).isEqualTo(expectedCaseDto)
  }

  @Test
  fun `should transform from case entity and person dto to case dto`() {
    val caseEntity = buildCaseEntity { withCrn(CRN) }
    val name = buildName()
    val personDto = buildFullPersonDto(crn = CRN, name = name)
    val eligibilityDto = buildEligibilityDto(CRN)
    val caseDto = buildCaseDto(crn = CRN, name = name.fullName)

    assertThat(personDto.toCaseDto(caseEntity = caseEntity, eligibility = eligibilityDto)).isEqualTo(caseDto)
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
    assertThat(cpr.toFullName()).isEqualTo(expected)
  }

  private companion object {
    private const val CRN = "X12345"

    private val caseDtoWhenAllDataSupplied = CaseDto(
      name = "First Middle Last",
      dateOfBirth = LocalDate.of(2000, 12, 3),
      crn = CRN,
      prisonNumber = "PRI1",
      tierScore = TierScore.A1,
      riskLevel = RiskLevel.VERY_HIGH,
      pncReference = "Some PNC Reference",
      assignedTo = AssignedToDto(
        forename = "First",
        surname = "Last",
        username = "user1",
        staffCode = "ABCD1234",
      ),
      photoUrl = null,
      currentAccommodation = null,
      nextAccommodation = null,
      status = null,
      actions = emptyList(),
      laoStatus = LAOStatus.NONE,
    )

    @JvmStatic
    fun caseTransformationCases(): Stream<Arguments> {
      val caseWithAllData = buildCaseOrchestrationDto(crn = CRN)
      val caseWithCprWithNoIdentifiers = caseWithAllData.copy(
        cpr = buildCorePersonRecord(identifiers = null),
      )
      val caseWithCprWithEmptyPrisonNumberAndPncsIdentifiers = caseWithAllData.copy(
        cpr = buildCorePersonRecord(
          identifiers = buildIdentifiers(
            crns = listOf(CRN),
            prisonNumbers = emptyList(),
            pncs = emptyList(),
          ),
        ),
      )

      val expectedCaseWithoutPrisonNumberData = caseDtoWhenAllDataSupplied.copy(
        prisonNumber = null,
        pncReference = null,
      )

      return Stream.of(
        Arguments.of(caseWithAllData, caseDtoWhenAllDataSupplied),
        Arguments.of(caseWithCprWithNoIdentifiers, expectedCaseWithoutPrisonNumberData),
        Arguments.of(caseWithCprWithEmptyPrisonNumberAndPncsIdentifiers, expectedCaseWithoutPrisonNumberData),
      )
    }
  }
}
