package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingPremises
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner

data class AccommodationOrchestrationDto(
  val cpr: CorePersonRecord?,
  val cas1Application: Cas1ApplicationV2?,
  val cas1Residence: Cas1PlacementV2?,
  val cas3CurrentPremises: Cas3BookingPremises?,
  val cas3Application: Cas3Application?,
  val prisoner: Prisoner?,
)
