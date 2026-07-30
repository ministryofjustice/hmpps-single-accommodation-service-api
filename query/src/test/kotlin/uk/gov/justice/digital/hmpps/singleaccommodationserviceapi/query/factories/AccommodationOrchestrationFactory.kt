package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPrisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationOrchestrationDto

fun buildAccommodationOrchestrationDto(
  cpr: CorePersonRecord? = buildCorePersonRecord(),
  cas1Application: Cas1Application? = null,
  cas3Application: Cas3Application? = null,
  cas1CurrentPremises: Cas1PremisesSummary? = null,
  cas3CurrentPremises: Cas3PremisesSummary? = null,
  prisoner: Prisoner? = buildPrisoner(),
) = AccommodationOrchestrationDto(
  cpr = cpr,
  cas1Application = cas1Application,
  cas1CurrentPremises = cas1CurrentPremises,
  cas3CurrentPremises = cas3CurrentPremises,
  cas3Application = cas3Application,
  prisoner = prisoner,
)
