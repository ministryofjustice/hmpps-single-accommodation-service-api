package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ExternalRequestForPlacementOutcome
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.RoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SexualOffenceRegistrations
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import java.time.LocalDate

data class DomainData(
  val crn: String,
  val tierScore: String?,
  val dob: LocalDate?,
  val sex: SexCode?,
  val roshLevel: RoshLevel?,
  val currentAccommodation: AccommodationSummaryDto?,
  val currentAccommodationTypeEntity: AccommodationTypeEntity?,
  val nextAccommodations: List<AccommodationSummaryDto>,
  val cas1Application: Cas1ApplicationDomainData?,
  val cas2Application: Cas2Application?,
  val cas3Application: Cas3Application?,
  val dutyToRefer: DutyToReferDto?,
  val commissionedRehabilitativeServices: CommissionedRehabilitativeServices?,
  val sexualOffenceRegistrations: SexualOffenceRegistrations?,
)

data class Cas1ApplicationDomainData(
  val application: Cas1ApplicationV2,
  val currentOutcome: Cas1ExternalRequestForPlacementOutcome?,
  val historyExcludingCurrentOutcome: List<Cas1ExternalRequestForPlacementOutcome>,
)
