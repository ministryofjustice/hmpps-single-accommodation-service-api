package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionNode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DecisionTreeBuilder
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion.Cas3CompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion.Cas3CompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.Cas3EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3SuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.validation.Cas3ValidationRuleSet

@Component
class Cas3EligibilityTreeProvider(
  private val builder: DecisionTreeBuilder,
  private val validation: Cas3ValidationRuleSet,
  private val upcoming: Cas3UpcomingRuleSet,
  private val upcomingContextUpdater: Cas3UpcomingContextUpdater,
  private val suitability: Cas3SuitabilityRuleSet,
  private val suitabilityContextUpdater: Cas3SuitabilityContextUpdater,
  private val completion: Cas3CompletionRuleSet,
  private val completionContextUpdater: Cas3CompletionContextUpdater,
  private val eligibility: Cas3EligibilityRuleSet,
) : EligibilityTreeProvider {

  private val tree: DecisionNode by lazy { build() }

  override fun tree(): DecisionNode = tree

  override fun initialContext(data: DomainData): EvaluationContext = EvaluationContext(
    data = data,
    currentResult = ServiceResult(
      serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
      link = EligibilityKeys.VIEW_REFERRAL,
    ),
  )

  private fun build(): DecisionNode {
    val confirmed = builder.confirmed()
    val notEligible = builder.notEligible()
    val bookingConfirmed = builder.outcome(
      ServiceResult(
        serviceStatus = ServiceStatus.BOOKING_CONFIRMED,
        link = EligibilityKeys.VIEW_REFERRAL,
      ),
    )

    val eligibilityNode = builder
      .ruleSet("Cas3Eligibility", eligibility)
      .onPass(confirmed)
      .onFail(notEligible)
      .build()

    val completionNode = builder
      .ruleSet("Cas3Completion", completion, completionContextUpdater)
      .onPass(bookingConfirmed)
      .onFail(confirmed)
      .build()

    val suitabilityNode = builder
      .ruleSet("Cas3Suitability", suitability, suitabilityContextUpdater)
      .onPass(completionNode)
      .onFail(eligibilityNode)
      .build()

    val upcomingNode = builder
      .ruleSet("Cas3Upcoming", upcoming, upcomingContextUpdater)
      .onPass(suitabilityNode)
      .onFail(eligibilityNode)
      .build()

    return builder
      .ruleSet("Cas3Validation", validation)
      .onPass(upcomingNode)
      .onFail(notEligible)
      .build()
  }
}
