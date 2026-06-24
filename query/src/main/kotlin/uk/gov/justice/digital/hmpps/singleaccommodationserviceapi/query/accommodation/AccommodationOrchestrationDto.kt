package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner

data class AccommodationOrchestrationDto(
  val cpr: CorePersonRecord?,
  val cas1Application: Cas1Application?,
  val cas1CurrentPremises: Cas1PremisesSummary?,
  val cas3CurrentPremises: Cas3PremisesSummary?,
  val cas3Application: Cas3Application?,
  val prisoner: Prisoner?,
)
