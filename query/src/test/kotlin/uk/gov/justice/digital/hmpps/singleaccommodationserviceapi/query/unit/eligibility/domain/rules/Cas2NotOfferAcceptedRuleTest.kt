package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2SubmittedApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas2NotOfferAcceptedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas2NotOfferAcceptedRuleTest {
  private val rule = Cas2NotOfferAcceptedRule()
  private val description = "FAIL if the CAS2 assessment status is not Offer Accepted"

  @Test
  fun `offer accepted assessment passes`() {
    val result = rule.evaluate(
      buildDomainData(
        cas2Application = buildCas2Application(
          submittedApplication = buildCas2SubmittedApplicationSummary(latestAssessmentStatus = Cas2AssessmentStatus.OFFER_ACCEPTED),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @ParameterizedTest
  @EnumSource(value = Cas2AssessmentStatus::class, names = ["OFFER_ACCEPTED"], mode = EnumSource.Mode.EXCLUDE)
  fun `assessment where offer is not accepted fails`(status: Cas2AssessmentStatus) {
    val result = rule.evaluate(
      buildDomainData(
        cas2Application = buildCas2Application(
          submittedApplication = buildCas2SubmittedApplicationSummary(
            latestAssessmentStatus = status,
            offerDeclinedReason = "Declined".takeIf { status == Cas2AssessmentStatus.OFFER_DECLINED },
            cancelledReason = "Cancelled".takeIf { status == Cas2AssessmentStatus.CANCELLED },
          ),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `missing assessment status fails`() {
    val result = rule.evaluate(
      buildDomainData(
        cas2Application = buildCas2Application(
          submittedApplication = buildCas2SubmittedApplicationSummary(latestAssessmentStatus = null),
        ),
      ),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `missing submitted application fails`() {
    val result = rule.evaluate(
      buildDomainData(cas2Application = buildCas2Application(submittedApplication = null)),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `missing application fails`() {
    val result = rule.evaluate(buildDomainData(cas2Application = null))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }
}
