package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier

data class CaseMutationOrchestrationDto(
  val crn: String,
  val cpr: CorePersonRecord?,
  val tier: Tier?,
  val cas1Application: Cas1Application?,
  val cas1CurrentPremises: Cas1PremisesSummary? = null,
  val cas3CurrentPremises: Cas3PremisesSummary? = null,
  val cas3Application: Cas3Application? = null,
  val prisoner: Prisoner? = null,
)
