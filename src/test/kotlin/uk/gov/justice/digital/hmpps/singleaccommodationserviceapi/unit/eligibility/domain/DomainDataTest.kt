package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildSex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class DomainDataTest {
  private val male = buildSex(SexCode.M)

  @Test
  fun `Create DomainDate from EligibilityOrchestration data`() {
    val crn = "ABC1234"
    val releaseDate = LocalDate.parse("2020-12-04")
    val expected = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = male,
      releaseDate = releaseDate,
      cas1Application = Cas1Application(
        UUID.randomUUID(),
        Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        null,
      ),
    )
    val cpr = CorePersonRecord(
      sex = expected.sex,
    )
    val tier = Tier(
      tierScore = expected.tier,
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
    )
    assertThat(result).isEqualTo(expected)
  }
}
