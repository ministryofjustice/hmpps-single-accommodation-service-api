package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildPersonDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class DomainDataTest {

  @Test
  fun `Create DomainDate from external data`() {
    val crn = "ABC1234"
    val releaseDate = LocalDate.parse("2020-12-04")
    val dutyToRefer = buildDutyToReferDto(crn = crn)
    val currentAccommodation = buildAccommodationDetail()
    val nextAccommodation = buildAccommodationDetail()

    val expected = buildDomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = SexCode.M,
      releaseDate = releaseDate,
      cas1Application = Cas1Application(
        UUID.randomUUID(),
        Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        null,
        null,
      ),
      cas3Application = Cas3Application(
        UUID.randomUUID(),
        applicationStatus = Cas3ApplicationStatus.SUBMITTED,
        bookingStatus = null,
        assessmentStatus = null,
      ),
      currentAccommodationArrangementType = currentAccommodation.arrangementType,
      currentAccommodationEndDate = currentAccommodation.endDate,
      hasNextAccommodation = true,
      dtrStatus = dutyToRefer.status,
      dtrSubmissionDate = dutyToRefer.submission?.submissionDate,
      dutyToReferData = dutyToRefer,
    )
    val cpr = CorePersonRecord(
      sex = buildSex(expected.sex),
    )
    val tier = Tier(
      tierScore = expected.tierScore!!,
      calculationId = UUID.randomUUID(),
      calculationDate = LocalDateTime.now(),
      changeReason = null,
    )
    val prisonerData = listOf(
      Prisoner(
        releaseDate = null,
      ),
      Prisoner(
        releaseDate = releaseDate.minusDays(1),
      ),
      Prisoner(
        releaseDate = releaseDate,
      ),
      Prisoner(
        releaseDate = releaseDate.minusDays(2),
      ),
    )

    val result = DomainData(
      crn = expected.crn,
      cpr = cpr,
      tier = tier,
      prisonerData = prisonerData,
      cas1Application = expected.cas1Application,
      cas3Application = expected.cas3Application,
      crsStatus = expected.crsStatus,
      dutyToRefer = dutyToRefer,
      currentAccommodation = currentAccommodation,
      nextAccommodation = nextAccommodation,
    )

    assertThat(result).isEqualTo(expected)
  }

  @Test
  fun `Create DomainDate from internal data`() {
    val crn = "ABC1234"
    val tierScore = TierScore.A1
    val releaseDate = LocalDate.parse("2020-12-04")
    val cas1Application = Cas1Application(
      UUID.randomUUID(),
      Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      null,
      null,
    )
    val personDto = buildPersonDto(
      crn = crn,
      expectedReleaseDate = releaseDate,
      gender = "Male",
    )
    val caseEntity = buildCaseEntity(
      tierScore = tierScore,
      cas1ApplicationId = cas1Application.id,
      cas1ApplicationApplicationStatus = cas1Application.applicationStatus,
      cas1ApplicationPlacementStatus = cas1Application.placementStatus,
      cas1ApplicationRequestForPlacementStatus = cas1Application.requestForPlacementStatus,
    ) { withCrn(crn) }
    val dutyToRefer = buildDutyToReferDto(crn = crn)
    val expected = buildDomainData(
      crn = crn,
      tierScore = tierScore,
      sex = SexCode.M,
      releaseDate = releaseDate,
      cas1Application = cas1Application,
      cas3Application = null,
      currentAccommodationArrangementType = null,
      currentAccommodationEndDate = null,
      hasNextAccommodation = false,
      dtrStatus = dutyToRefer.status,
      dtrSubmissionDate = dutyToRefer.submission?.submissionDate,
      crsStatus = null,
      dutyToReferData = dutyToRefer,
    )
    val result = DomainData(
      personDto = personDto,
      caseEntity = caseEntity,
      dutyToRefer = dutyToRefer,
    )
    assertThat(result).isEqualTo(expected)
  }
}
