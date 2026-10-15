package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.persistence.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withPrisonNumber

class CaseEntityTest {

  @Test
  fun `latestCrn ignores archived CRNs`() {
    val case = buildCaseEntity {
      withCrn("ACTIVE-CRN")
      withCrn("ARCHIVED-CRN")
      archiveCrn("ARCHIVED-CRN")
    }
    assertThat(case.latestCrn()).isEqualTo("ACTIVE-CRN")
  }

  @Test
  fun `latestPrisonNumber ignores archived prison numbers`() {
    val case = buildCaseEntity {
      withPrisonNumber("ACTIVE-PN")
      withPrisonNumber("ARCHIVED-PN")
      archivePrisonNumber("ARCHIVED-PN")
    }
    assertThat(case.latestPrisonNumber()).isEqualTo("ACTIVE-PN")
  }
}
