package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class DomainDataTest {

  @Test
  fun `Create DomainDate from EligibilityOrchestration data`() {
    val crn = "ABC1234"
    val releaseDate = LocalDate.parse("2020-12-04")
    val expected = DomainData(
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
    )
    assertThat(result).isEqualTo(expected)
  }
}
