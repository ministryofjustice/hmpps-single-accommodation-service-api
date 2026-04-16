package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.completion

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion.Cas3ApplicationCompletionRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.LocalDate

class Cas3ApplicationCompletionRuleTest {
  private val crn = "ABC234"
  private val male = SexCode.M
  private val tierScore = TierScore.A1
  private val description = "FAIL if CAS3 application is not complete"

  @Test
  fun `application is completed so rule passes`() {
    val data = buildDomainData(
      crn = crn,
      tierScore = tierScore,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas3Application = buildCas3Application(
        applicationStatus = Cas3ApplicationStatus.SUBMITTED,
        assessmentStatus = Cas3AssessmentStatus.READY_TO_PLACE,
        bookingStatus = Cas3BookingStatus.CONFIRMED,
      ),
    )

    val result = Cas3ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3BookingStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["CONFIRMED"])
  fun `application is not completed so rule fails`(bookingStatus: Cas3BookingStatus) {
    val data = buildDomainData(
      crn = crn,
      tierScore = tierScore,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas3Application = buildCas3Application(
        applicationStatus = Cas3ApplicationStatus.SUBMITTED,
        assessmentStatus = Cas3AssessmentStatus.READY_TO_PLACE,
        bookingStatus = bookingStatus,
      ),
    )

    val result = Cas3ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @Test
  fun `rule has correct description`() {
    assertThat(Cas3ApplicationCompletionRule().description).isEqualTo(description)
  }
}
