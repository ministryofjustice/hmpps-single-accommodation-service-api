package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildSex
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

class DomainDataTest {
  private val male = buildSex(SexCode.M)

  @Test
  fun `Create DomainDate from EligibilityOrchestration data`() {
    val crn = "ABC1234"
    val releaseDate = OffsetDateTime.parse("2020-12-04T00:00:00+00:00")
    val localReleaseDate = releaseDate.toLocalDate()
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
        releaseDate = localReleaseDate.minusDays(1),
      ),
      Prisoner(
        releaseDate = localReleaseDate,
      ),
      Prisoner(
        releaseDate = localReleaseDate.minusDays(2),
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
