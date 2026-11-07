package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingPremises
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPrisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationOrchestrationDto

fun buildAccommodationOrchestrationDto(
  cpr: CorePersonRecord? = buildCorePersonRecord(),
  cas1Application: Cas1ApplicationV2? = null,
  cas3Application: Cas3Application? = null,
  cas1Residence: Cas1PlacementV2? = null,
  cas3CurrentPremises: Cas3BookingPremises? = null,
  prisoner: Prisoner? = buildPrisoner(),
) = AccommodationOrchestrationDto(
  cpr = cpr,
  cas1Application = cas1Application,
  cas1Residence = cas1Residence,
  cas3CurrentPremises = cas3CurrentPremises,
  cas3Application = cas3Application,
  prisoner = prisoner,
)
