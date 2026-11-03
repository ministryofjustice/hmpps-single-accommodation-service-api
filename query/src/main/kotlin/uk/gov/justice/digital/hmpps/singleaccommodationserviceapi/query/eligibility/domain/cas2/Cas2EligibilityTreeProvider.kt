package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.arrived.Cas2ArrivedContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.arrived.Cas2ArrivedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.arrived.Cas2NotArrivedContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.assessment.Cas2AssessmentRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.assessment.Cas2NotActiveContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.CurrentAccommodationEligibleForCas2RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.PersonEligibleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.ReleaseDateEligibleRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.Cas2RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.Cas2StartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.submittedReferral.Cas2NotSubmittedContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.submittedReferral.Cas2SubmittedRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.upcoming.Cas2UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NoNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.ReleaseWithin15DaysRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.RoshEligibleRuleSet

@Component
class Cas2EligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val releaseWithin15Days: ReleaseWithin15DaysRuleSet,
  private val upcomingContextUpdater: Cas2UpcomingContextUpdater,
  private val referral: Cas2RuleSet,
  private val startContextUpdater: Cas2StartContextUpdater,
  private val personEligible: PersonEligibleRuleSet,
  private val releaseDateEligible: ReleaseDateEligibleRuleSet,
  private val currentAccommodationEligible: CurrentAccommodationEligibleForCas2RuleSet,
  private val noNextAccommodation: NoNextAccommodationRuleSet,
  private val roshEligible: RoshEligibleRuleSet,
  private val submitted: Cas2SubmittedRuleSet,
  private val notSubmittedContextUpdater: Cas2NotSubmittedContextUpdater,
  private val assessment: Cas2AssessmentRuleSet,
  private val notActiveContextUpdater: Cas2NotActiveContextUpdater,
  private val arrived: Cas2ArrivedRuleSet,
  private val arrivedContextUpdater: Cas2ArrivedContextUpdater,
  private val notArrivedContextUpdater: Cas2NotArrivedContextUpdater,
) : EligibilityTreeProvider {

  private val tree: DecisionNode by lazy { build() }

  override fun tree(): DecisionNode = tree

  override fun initialContext(data: DomainData): EvaluationContext = EvaluationContext(
    data = data,
    currentResult = null,
  )

  private fun build(): DecisionNode {
    val confirmed = builder.confirmed()
    val notEligible = builder.notEligible(ServiceType.CAS2)

    val roshEligibleNode = builder
      .ruleSet(roshEligible)
      .onPass(confirmed)
      .onFail(notEligible)
      .build()

    val noNextAccommodationNode = builder
      .ruleSet(noNextAccommodation)
      .onPass(roshEligibleNode)
      .onFail(notEligible)
      .build()

    val currentAccommodationEligibleNode = builder
      .ruleSet(currentAccommodationEligible)
      .onPass(noNextAccommodationNode)
      .onFail(notEligible)
      .build()

    val releaseDateEligibleNode = builder
      .ruleSet(releaseDateEligible)
      .onPass(currentAccommodationEligibleNode)
      .onFail(notEligible)
      .build()

    val personEligibleNode = builder
      .ruleSet(personEligible)
      .onPass(releaseDateEligibleNode)
      .onFail(notEligible)
      .build()

    val arrivedNode = builder
      .ruleSet(arrived)
      .onPass(confirmed).applyContextUpdaterOnPass(arrivedContextUpdater)
      .onFail(confirmed).applyContextUpdaterOnFailure(notArrivedContextUpdater)
      .build()

    val assessmentNode = builder
      .ruleSet(assessment)
      .onPass(arrivedNode)
      .onFail(personEligibleNode).applyContextUpdaterOnFailure(notActiveContextUpdater)
      .build()

    val submittedNode = builder
      .ruleSet(submitted)
      .onPass(assessmentNode)
      .onFail(personEligibleNode).applyContextUpdaterOnFailure(notSubmittedContextUpdater)
      .build()

    val referralNode = builder
      .ruleSet(referral)
      .onPass(submittedNode)
      .onFail(personEligibleNode).applyContextUpdaterOnFailure(startContextUpdater)
      .build()

    return builder
      .ruleSet(releaseWithin15Days)
      .onPass(referralNode)
      .onFail(personEligibleNode).applyContextUpdaterOnFailure(upcomingContextUpdater)
      .build()
  }
}
