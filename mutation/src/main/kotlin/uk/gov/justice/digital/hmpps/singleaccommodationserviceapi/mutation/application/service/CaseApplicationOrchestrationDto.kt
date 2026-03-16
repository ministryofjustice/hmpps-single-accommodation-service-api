package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2CourtBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2HdcApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2PrisonBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier

data class CaseApplicationOrchestrationDto(
  val crn: String,
  val cpr: CorePersonRecord?,
  val roshDetails: RoshDetails?,
  val tier: Tier?,
  val cas1Application: Cas1Application?,
  val cas2HdcApplication: Cas2HdcApplication?,
  val cas2PrisonBailApplication: Cas2PrisonBailApplication?,
  val cas2CourtBailApplication: Cas2CourtBailApplication?,
)
