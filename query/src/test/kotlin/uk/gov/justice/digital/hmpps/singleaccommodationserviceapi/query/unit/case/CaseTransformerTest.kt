package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.NullSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.SexualOffenceRegistrationCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummariesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.OffenceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SexualOffenceRegistration
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSexualOffenceRegistration
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSexualOffenceRegistrations
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toLimitedCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCaseOrchestrationDto
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SexualOffenceRegistrationCode as UpstreamRegistrationCode

class CaseTransformerTest {
  private val crn = UUID.randomUUID().toString()

  @Test
  fun `returns UserAccess of UNKNOWN when case is missing`() {
    val result = toCaseDto(crn = crn, case = null, cpr = null, tier = null, sexualOffenceRegistrations = null, userAccess = null)

    assertThat(result.crn).isEqualTo(crn)
    assertUserAccess(result, UserAccess.UNKNOWN)
  }

  @Test
  fun `returns UserAccess FULL when user has full access`() {
    val result = toCaseDto(crn = crn, case = buildCase(crn = crn), cpr = null, tier = null, sexualOffenceRegistrations = null, userAccess = UserAccess.FULL)

    assertThat(result.crn).isEqualTo(crn)
    assertUserAccess(result, UserAccess.FULL)
  }

  @Test
  fun `returns UserAccess of FULL when user has full access and limitedAccess is true`() {
    val result = toCaseDto(
      crn = crn,
      case = buildCase(crn = crn, limitedAccess = true),
      cpr = null,
      tier = null,
      sexualOffenceRegistrations = null,
      userAccess = UserAccess.FULL,
    )

    assertThat(result.crn).isEqualTo(crn)
    assertUserAccess(result, UserAccess.FULL, limitedAccess = true)
  }

  @ParameterizedTest
  @EnumSource(UserAccess::class, names = ["LIMITED", "UNKNOWN"])
  @NullSource
  fun `returns UserAccess of LIMITED when user does not have full access`(userAccess: UserAccess?) {
    val result = toCaseDto(crn = crn, case = buildCase(crn = crn), cpr = null, tier = null, sexualOffenceRegistrations = null, userAccess = userAccess)

    assertThat(result.crn).isEqualTo(crn)
    assertUserAccess(result, UserAccess.LIMITED, limitedAccess = true)
  }

  @Test
  fun `uses crn and prisonNumber from case`() {
    val prisonNumber = "prisonNumber1"
    val identifiers = buildIdentifiers(
      crns = listOf(UUID.randomUUID().toString(), UUID.randomUUID().toString(), crn),
      prisonNumbers = listOf(UUID.randomUUID().toString(), prisonNumber),
    )

    val result = toCaseDto(
      crn = crn,
      case = buildCase(crn = crn, nomsNumber = prisonNumber),
      cpr = buildCorePersonRecord(identifiers = identifiers),
      tier = null,
      sexualOffenceRegistrations = null,
      userAccess = UserAccess.FULL,
    )

    assertThat(result.crn).isEqualTo(crn)
    assertThat(result.prisonNumber).isEqualTo(prisonNumber)
    assertUserAccess(result, UserAccess.FULL)
  }

  @Test
  fun `defaults limitedAccess to false when delius does not send it`() {
    val result = toCaseDto(
      crn = crn,
      case = buildCase(crn = crn).copy(limitedAccess = null),
      cpr = null,
      tier = null,
      sexualOffenceRegistrations = null,
      userAccess = UserAccess.FULL,
    )

    assertThat(result.limitedAccess).isFalse()
  }

  @Test
  fun `returns null riskLevel when rosh level code is unknown`() {
    val result = toCaseDto(
      crn = crn,
      case = buildCase(crn = crn, roshLevel = buildRoshLevel(code = "UNKNOWN")),
      cpr = null,
      tier = null,
      sexualOffenceRegistrations = null,
      userAccess = UserAccess.FULL,
    )

    assertThat(result.riskLevel).isNull()
  }

  @Test
  fun `returns null riskLevel when rosh level is missing`() {
    val result = toCaseDto(
      crn = crn,
      case = buildCase(crn = crn).copy(roshLevel = null),
      cpr = null,
      tier = null,
      sexualOffenceRegistrations = null,
      userAccess = UserAccess.FULL,
    )

    assertThat(result.riskLevel).isNull()
  }

  @Test
  fun `should transform case orchestration dto to case dto when all data supplied`() {
    assertCaseOrchestrationTransform(orchestratedCaseDto, expectedCaseDto)
  }

  @Test
  fun `maps every sexual offence registration code preserving order and duplicates`() {
    val codes = UpstreamRegistrationCode.entries.reversed() + UpstreamRegistrationCode.ARSO
    val registrations = buildSexualOffenceRegistrations(
      crn = crn,
      sexualOffenceRegistrations = codes.map { code ->
        buildSexualOffenceRegistration(type = OffenceType(code, code.description))
      },
    )

    assertCaseOrchestrationTransform(
      orchestratedCaseDto.copy(sexualOffenceRegistrations = registrations),
      expectedCaseDto.copy(
        sexualOffenceRegistrations = listOf(
          SexualOffenceRegistrationCode.SOPS,
          SexualOffenceRegistrationCode.ALT3,
          SexualOffenceRegistrationCode.ALT13,
          SexualOffenceRegistrationCode.CSEP,
          SexualOffenceRegistrationCode.SHPO,
          SexualOffenceRegistrationCode.RCHD,
          SexualOffenceRegistrationCode.ARSO,
          SexualOffenceRegistrationCode.ANSO,
          SexualOffenceRegistrationCode.RSC,
          SexualOffenceRegistrationCode.ARSO,
        ),
      ),
    )
  }

  @ParameterizedTest
  @NullAndEmptySource
  fun `null or empty registration list maps to an empty list`(registrations: List<SexualOffenceRegistration>?) {
    assertCaseOrchestrationTransform(
      orchestratedCaseDto.copy(
        sexualOffenceRegistrations = buildSexualOffenceRegistrations(crn, registrations),
      ),
      expectedCaseDto,
    )
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

  @Test
  fun `should use the accommodation from the case entity when transforming to case dto`() {
    val currentAccommodationDto = buildAccommodationSummaryDto(crn = crn)
    val nextAccommodationDto = buildAccommodationSummaryDto(crn = crn)
    val caseEntity = buildCaseEntity {
      withCrn(crn)
      accommodationStatus = CaseAccommodationStatus.SETTLED
      accommodationStatusDate = LocalDate.now()
      currentAccommodation = currentAccommodationDto
      nextAccommodation = nextAccommodationDto
    }

    val result = toCaseDto(
      caseEntity = caseEntity,
      crn = crn,
      assignedTo = null,
      limitedAccess = false,
      teamCode = "TEAM1",
    )

    assertThat(result.accommodationSummaries).isEqualTo(
      buildAccommodationSummariesDto(
        caseAccommodationStatus = CaseAccommodationStatus.SETTLED,
        caseAccommodationStatusDate = LocalDate.now(),
        currentAccommodation = currentAccommodationDto,
        nextAccommodation = nextAccommodationDto,
      ),
    )
  }

  @Test
  fun `should transform from case entity and supplied person fields to case dto`() {
    val accommodationSummaries = buildAccommodationSummariesDto(
      caseAccommodationStatus = CaseAccommodationStatus.TRANSIENT,
      caseAccommodationStatusDate = LocalDate.now(),
    )
    val caseEntity = buildCaseEntity(roshLevelCode = RiskLevel.HIGH.code, accommodationSummariesDto = accommodationSummaries) {
      withCrn(crn)
      withPrisonNumber("A1234AA")
    }
    val assignedTo = AssignedToDto(forename = "Staff", surname = "Member", username = "staff.member")

    val result = toCaseDto(
      caseEntity = caseEntity,
      crn = crn,
      assignedTo = assignedTo,
      limitedAccess = false,
      teamCode = "TeamX",
    )

    assertThat(result).isEqualTo(
      CaseDto(
        forename = caseEntity.firstName,
        middleNames = null,
        surname = caseEntity.lastName,
        dateOfBirth = caseEntity.dateOfBirth,
        crn = crn,
        prisonNumber = "A1234AA",
        photoUrl = null,
        tierScore = caseEntity.tierScore,
        riskLevel = RiskLevel.HIGH,
        assignedTo = assignedTo,
        userAccess = UserAccess.FULL,
        limitedAccess = false,
        accommodationSummaries = accommodationSummaries,
        teamCode = "TeamX",
      ),
    )
  }

  @Test
  fun `should transform to case dto with only the supplied person fields when there is no case entity`() {
    val result = toCaseDto(
      caseEntity = null,
      crn = crn,
      assignedTo = null,
      limitedAccess = null,
      teamCode = "TeamX",
    )

    assertThat(result).isEqualTo(CaseDto(crn = crn, userAccess = UserAccess.FULL, limitedAccess = null, teamCode = "TeamX"))
  }

  @Test
  fun `should transform a crn to a limited case dto`() {
    val assignedTo = AssignedToDto(forename = "Staff", surname = "Member")
    assertThat(
      toLimitedCaseDto(crn, teamCode = "TeamX", assignedTo = assignedTo),
    ).isEqualTo(
      CaseDto(
        crn = crn,
        userAccess = UserAccess.LIMITED,
        limitedAccess = true,
        teamCode = "TeamX",
        assignedTo = assignedTo,
      ),
    )
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
      case = caseOrchestrationDto.case,
      sexualOffenceRegistrations = caseOrchestrationDto.sexualOffenceRegistrations,
      userAccess = UserAccess.FULL,
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
    tierScore = "A",
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
