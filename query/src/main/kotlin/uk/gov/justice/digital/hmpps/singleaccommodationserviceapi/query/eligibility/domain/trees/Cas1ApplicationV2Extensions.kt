package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ExternalRequestForPlacementOutcomeDto
import java.time.LocalDateTime

/** The latest of the request for placement and placement status update times */
val Cas1ExternalRequestForPlacementOutcomeDto.statusLastUpdated: LocalDateTime
  get() = listOfNotNull(requestForPlacement.statusLastUpdated, placement?.statusLastUpdated).max()

val Cas1ApplicationV2.sortedRequestForPlacementHistory: List<Cas1ExternalRequestForPlacementOutcomeDto>
  get() = requestForPlacementHistory.sortedByDescending { it.statusLastUpdated }

/** The pending outcome if there is one, otherwise the most recently updated historic outcome */
val Cas1ApplicationV2.currentOutcome: Cas1ExternalRequestForPlacementOutcomeDto?
  get() = upcomingRequestForPlacement ?: sortedRequestForPlacementHistory.firstOrNull()
