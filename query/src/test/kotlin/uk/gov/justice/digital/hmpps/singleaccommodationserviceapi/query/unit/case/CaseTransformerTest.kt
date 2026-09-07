package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummariesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDtoV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonTransformer.toPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildFullPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildLimitedPersonDto
import java.time.LocalDate
import java.util.UUID

class CaseTransformerTest {
  private val crn = UUID.randomUUID().toString()

  @Test
  fun `returns UserAccess of UNKNOWN when personDto is missing`() {
    val result = toCaseDto(crn = crn, person = null, cpr = null, tier = null)

    assertThat(result.crn).isEqualTo(crn)
    assertUserAccess(result, UserAccess.UNKNOWN)
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `returns UserAccess FULL when personDto is FullPersonDto`(v2Enabled: Boolean) {
    val person = buildFullPersonDto(crn)

    val fromOrchestration = toCaseDto(crn = crn, person = person, cpr = null, tier = null)
    val fromSasAndDelius = if (v2Enabled) {
      person.toCaseDtoV2(caseEntity = null, currentAccommodation = null, nextAccommodation = null)
    } else {
      person.toCaseDto(caseEntity = null)
    }

    assertThat(fromOrchestration.crn).isEqualTo(crn)
    assertUserAccess(fromOrchestration, UserAccess.FULL)
    assertUserAccess(fromSasAndDelius, UserAccess.FULL)
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `returns UserAccess of FULL when personDto is FullPersonDto and limitedAccess is true`(v2Enabled: Boolean) {
    val person = buildFullPersonDto(crn, limitedAccess = true)

    val fromOrchestration = toCaseDto(crn = crn, person = person, cpr = null, tier = null)
    val fromSasAndDelius = if (v2Enabled) {
      person.toCaseDtoV2(caseEntity = null, currentAccommodation = null, nextAccommodation = null)
    } else {
      person.toCaseDto(caseEntity = null)
    }

    assertThat(fromOrchestration.crn).isEqualTo(crn)
    assertUserAccess(fromOrchestration, UserAccess.FULL, limitedAccess = true)
    assertUserAccess(fromSasAndDelius, UserAccess.FULL, limitedAccess = true)
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `returns UserAccess of LIMITED when personDto is LimitedPersonDto`(v2Enabled: Boolean) {
    val person = buildLimitedPersonDto(crn)

    val fromOrchestration = toCaseDto(crn = crn, person = person, cpr = null, tier = null)
    val fromSasAndDelius = if (v2Enabled) {
      person.toCaseDtoV2(caseEntity = null, currentAccommodation = null, nextAccommodation = null)
    } else {
      person.toCaseDto(caseEntity = null)
    }

    assertThat(fromOrchestration.crn).isEqualTo(crn)
    assertUserAccess(fromOrchestration, UserAccess.LIMITED, limitedAccess = true)
    assertUserAccess(fromSasAndDelius, UserAccess.LIMITED, limitedAccess = true)
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `uses crn and prisonNumber from personDto`(v2Enabled: Boolean) {
    val prisonNumber = "prisonNumber1"
    val person = buildFullPersonDto(crn = crn, nomsNumber = prisonNumber)
    val identifiers = buildIdentifiers(
      crns = listOf(UUID.randomUUID().toString(), UUID.randomUUID().toString(), crn),
      prisonNumbers = listOf(UUID.randomUUID().toString(), prisonNumber),
    )

    val fromOrchestration =
      toCaseDto(crn = crn, person = person, cpr = buildCorePersonRecord(identifiers = identifiers), tier = null)
    assertThat(fromOrchestration.crn).isEqualTo(crn)
    assertThat(fromOrchestration.prisonNumber).isEqualTo(prisonNumber)
    assertUserAccess(fromOrchestration, UserAccess.FULL)

    val fromSasAndDelius = if (v2Enabled) {
      person.toCaseDtoV2(caseEntity = null, currentAccommodation = null, nextAccommodation = null)
    } else {
      person.toCaseDto(caseEntity = null)
    }
    assertThat(fromSasAndDelius.crn).isEqualTo(crn)
    assertThat(fromSasAndDelius.prisonNumber).isEqualTo(prisonNumber)
    assertUserAccess(fromSasAndDelius, UserAccess.FULL)
  }

  @Test
  fun `should transform case orchestration dto to case dto when all data supplied`() {
    assertCaseOrchestrationTransform(orchestratedCaseDto, expectedCaseDto)
  }

  @Test
  fun `should transform case orchestration dto to case dto when cpr identifiers are missing`() {
    val caseWithNoIdentifiers = orchestratedCaseDto.copy(
      cpr = buildCorePersonRecord(identifiers = null),
    )

    assertCaseOrchestrationTransform(caseWithNoIdentifiers, expectedCaseWithoutPnc)
  }

  @Test
  fun `should transform case orchestration dto to case dto when prison and pnc identifiers are empty`() {
    val caseWithEmptyPrisonAndPncIdentifiers = orchestratedCaseDto.copy(
      cpr = buildCorePersonRecord(
        identifiers = buildIdentifiers(
          crns = listOf(crn),
          prisonNumbers = emptyList(),
          pncs = emptyList(),
        ),
      ),
    )

    assertCaseOrchestrationTransform(caseWithEmptyPrisonAndPncIdentifiers, expectedCaseWithoutPnc)
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `should transform from case entity and person dto to case dto`(v2Enabled: Boolean) {
    val currentAccommodationDto = buildAccommodationSummaryDto(crn = crn)
    val nextAccommodationDto = buildAccommodationSummaryDto(crn = crn)
    val caseEntity = buildCaseEntity {
      withCrn(crn)
      accommodationStatus = CaseAccommodationStatus.SETTLED
    }
    val name = buildName()
    val personDto = buildFullPersonDto(crn = crn, name = name)

    if (v2Enabled) {
      val expected = buildCaseDto(
        crn = crn,
        forename = caseEntity.firstName!!,
        middleNames = null,
        surname = caseEntity.lastName!!,
        dateOfBirth = caseEntity.dateOfBirth!!,
        tierScore = caseEntity.tierScore!!,
        accommodationSummaries = buildAccommodationSummariesDto(
          caseAccommodationStatus = CaseAccommodationStatus.SETTLED,
          caseAccommodationStatusDate = null,
          currentAccommodation = currentAccommodationDto,
          nextAccommodation = nextAccommodationDto,
        ),
      )
      assertThat(
        personDto.toCaseDtoV2(
          caseEntity = caseEntity,
          currentAccommodation = currentAccommodationDto,
          nextAccommodation = nextAccommodationDto,
        ),
      ).isEqualTo(expected)
    } else {
      val expected = buildCaseDto(
        crn = crn,
        forename = name.forename,
        middleNames = name.middleName,
        surname = name.surname,
        tierScore = caseEntity.tierScore!!,
      )
      assertThat(personDto.toCaseDto(caseEntity = caseEntity)).isEqualTo(expected)
    }
  }

  private fun assertUserAccess(caseDto: CaseDto, expectedAccess: UserAccess, limitedAccess: Boolean? = null) {
    assertThat(caseDto.userAccess).isEqualTo(expectedAccess)
    limitedAccess?.let { assertThat(caseDto.limitedAccess).isEqualTo(it) }
  }

  private fun assertCaseOrchestrationTransform(
    caseOrchestrationDto: CaseOrchestrationDto,
    expectedCaseDto: CaseDto,
  ) {
    val result = toCaseDto(
      crn = crn,
      cpr = caseOrchestrationDto.cpr,
      tier = caseOrchestrationDto.tier,
      person = toPersonDto(caseOrchestrationDto.case!!),
    )

    assertThat(result).isEqualTo(expectedCaseDto)
  }

  private val expectedCaseDto = CaseDto(
    forename = "First",
    middleNames = "Middle",
    surname = "Last",
    dateOfBirth = LocalDate.of(2000, 12, 3),
    crn = crn,
    prisonNumber = "YY09876Y",
    tierScore = "A1",
    riskLevel = RiskLevel.VERY_HIGH,
    pncReference = "Some PNC Reference",
    assignedTo = AssignedToDto(
      forename = "First",
      surname = "Last",
      username = "user1",
    ),
    photoUrl = null,
    userAccess = UserAccess.FULL,
    limitedAccess = false,
  )

  private val orchestratedCaseDto = buildCaseOrchestrationDto(crn = crn)

  private val expectedCaseWithoutPnc = expectedCaseDto.copy(
    pncReference = null,
  )
}
