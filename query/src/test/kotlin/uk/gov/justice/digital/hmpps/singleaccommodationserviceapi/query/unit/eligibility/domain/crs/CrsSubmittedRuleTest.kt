package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.crs

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.CrsStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsSubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.LocalDate

class CrsSubmittedRuleTest {
  private val description = "FAIL if CRS not submitted"

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = CrsStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["NSI_REFERRAL", "IN_PROGRESS", "NSI_TERMINATED"])
  fun `crs is submitted so rule passes`(crsStatus: CrsStatus) {
    val data = buildDomainData(
      commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices(
        status = crsStatus,
        submissionDate = LocalDate.now(),
      ),
    )

    val result = CrsSubmittedRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = CrsStatus::class, names = ["NSI_REFERRAL", "IN_PROGRESS", "NSI_TERMINATED"])
  fun `crs is not submitted so rule fails`(crsStatus: CrsStatus) {
    val data = buildDomainData(
      commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices(
        status = crsStatus,
        submissionDate = LocalDate.now(),
      ),
    )

    val result = CrsSubmittedRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @Test
  fun `crs is missing so rule fails`() {
    val data = buildDomainData(
      commissionedRehabilitativeServices = null,
    )

    val result = CrsSubmittedRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }
}
