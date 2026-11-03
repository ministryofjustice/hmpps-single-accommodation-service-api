package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ExternalRequestForPlacementOutcome
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1SubmittedApplication
import java.time.LocalDateTime

/** The latest of the request for placement and placement status update times */
val Cas1ExternalRequestForPlacementOutcome.statusLastUpdated: LocalDateTime
  get() = listOfNotNull(requestForPlacement.statusLastUpdated, placement?.statusLastUpdated).max()

val Cas1SubmittedApplication.sortedRequestForPlacementHistory: List<Cas1ExternalRequestForPlacementOutcome>
  get() = requestForPlacementHistory.sortedByDescending { it.statusLastUpdated }

/** The pending outcome if there is one, otherwise the most recently updated historic outcome */
val Cas1ApplicationV2.currentOutcome: Cas1ExternalRequestForPlacementOutcome?
  get() = submittedApplication?.let { it.upcomingRequestForPlacement ?: it.sortedRequestForPlacementHistory.firstOrNull() }

val Cas1ApplicationV2.historyExcludingCurrentOutcome: List<Cas1ExternalRequestForPlacementOutcome>
  get() = submittedApplication?.let { it.sortedRequestForPlacementHistory.filter { outcome -> outcome != currentOutcome } } ?: emptyList()
