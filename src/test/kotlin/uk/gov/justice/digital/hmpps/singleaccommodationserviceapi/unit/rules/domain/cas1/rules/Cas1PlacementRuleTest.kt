package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules.domain.cas1.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.ApprovedPremisesApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.Cas1PlacementRule
import java.time.OffsetDateTime
import java.util.UUID

class Cas1PlacementRuleTest {
  private val cas1PlacementRule = Cas1PlacementRule()
  private val male = Sex(
    code = "M",
    description = "Male",
  )

  @Test
  fun `candidate has no application so fails`() {
    val data = DomainData(
      tier = "A1",
      sex = male,
      releaseDate = OffsetDateTime.now().plusMonths(6),
    )
    val result = cas1PlacementRule.evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `candidate has an application but there is no placement allocated so fails`() {
    val data = DomainData(
      tier = "A1",
      sex = male,
      releaseDate = OffsetDateTime.now().plusMonths(6),
      cas1Application = Cas1Application(
        id = UUID.randomUUID(),
      ),
    )
    val result = cas1PlacementRule.evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `candidate has a submitted application so passes`() {
    val data = DomainData(
      tier = "A1",
      sex = male,
      releaseDate = OffsetDateTime.now().plusMonths(6),
      cas1Application = Cas1Application(
        id = UUID.randomUUID(),
        status = ApprovedPremisesApplicationStatus.PlacementAllocated,
      ),
    )
    val result = cas1PlacementRule.evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `rule has correct description`() {
    val result = cas1PlacementRule.description
    assertThat(result).isEqualTo("FAIL if candidate has no upcoming placement")
  }
}
