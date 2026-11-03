package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ExternalRequestForPlacementOutcomeDto

data class Cas1ApplicationDomainData(
  val application: Cas1ApplicationV2,
  val currentOutcome: Cas1ExternalRequestForPlacementOutcomeDto?,
)
