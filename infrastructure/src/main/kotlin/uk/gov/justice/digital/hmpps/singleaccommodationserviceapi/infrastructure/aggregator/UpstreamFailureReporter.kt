package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator

interface UpstreamFailureReporter {
  fun report(callKey: String, failure: AggregatorCallOutcome.Failure, cause: Exception)
}
