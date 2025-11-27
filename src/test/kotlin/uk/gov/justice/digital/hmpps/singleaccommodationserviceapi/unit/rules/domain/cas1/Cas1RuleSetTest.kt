package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.rules.domain.cas1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.description
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules.STierRule

class Cas1RuleSetTest {
  @Test
  fun `check ruleset contains STierRule`() {
    val result = Cas1RuleSet().getRules()

    assertThat(result.first().description).isEqualTo(STierRule().description)
  }

}