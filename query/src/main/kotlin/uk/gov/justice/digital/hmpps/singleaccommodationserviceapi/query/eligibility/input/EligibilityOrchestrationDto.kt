package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingPremises
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SexualOffenceRegistrations
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier

data class EligibilityOrchestrationDto(
  val crn: String,
  val cpr: CorePersonRecord?,
  val tier: Tier?,
  val cas1Application: Cas1ApplicationV2?,
  val cas2Application: Cas2Application?,
  val cas3Application: Cas3Application?,
  val commissionedRehabilitativeServices: List<CommissionedRehabilitativeServices>?,
  val cas1Residence: Cas1PlacementV2?,
  val cas3CurrentPremises: Cas3BookingPremises?,
  val prisoner: Prisoner?,
  val sexualOffenceRegistrations: SexualOffenceRegistrations?,
  val case: Case?,
)
