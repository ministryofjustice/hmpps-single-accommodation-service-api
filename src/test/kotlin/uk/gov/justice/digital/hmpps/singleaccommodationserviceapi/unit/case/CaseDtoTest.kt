package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.CaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildAccommodationDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildCaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildNoFixedAbodeAccommodationDetails
import java.time.LocalDate
import java.util.stream.Stream

class CaseDtoTest {

  @ParameterizedTest
  @MethodSource(
    "uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.case.CaseDtoTest#caseTransformationCases",
  )
  fun `should transform from case orchestration dto to case dto`(
    caseOrchestrationDto: CaseOrchestrationDto,
    expectedCaseDto: CaseDto,
  ) {
    assertThat(
      CaseDto(
        crn = caseOrchestrationDto.crn,
        cpr = caseOrchestrationDto.cpr,
        roshDetails = caseOrchestrationDto.roshDetails,
        tier = caseOrchestrationDto.tier,
        caseSummaries = caseOrchestrationDto.cases,
        photoUrl = caseOrchestrationDto.photoUrl,
        accommodationDto = caseOrchestrationDto.accommodationDto,
      ),
    ).isEqualTo(expectedCaseDto)
  }

  private companion object {
    private const val CRN = "X12345"

    private val caseDtoWhenAllDataSupplied = CaseDto(
      name = "First Middle Last",
      dateOfBirth = LocalDate.of(2000, 12, 3),
      crn = CRN,
      prisonNumber = "PRI1",
      photoUrl = "!!https://www.replace-this-with-a-real-url.com",
      tier = TierScore.C1,
      riskLevel = RiskLevel.VERY_HIGH,
      pncReference = "Some PNC Reference",
      assignedTo = AssignedToDto(1L, name = "Team 1"),
      currentAccommodation = buildAccommodationDetails(),
      nextAccommodation = buildNoFixedAbodeAccommodationDetails(),
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
