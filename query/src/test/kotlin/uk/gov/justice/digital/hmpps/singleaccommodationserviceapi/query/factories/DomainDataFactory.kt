package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Cas1ApplicationDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DomainData
import java.time.LocalDate

fun buildDomainData(
  crn: String = "CR12345N",
  tierScore: String? = null,
  sex: SexCode? = null,
  dob: LocalDate? = null,
  roshLevel: RoshLevel? = null,
  currentAccommodation: AccommodationSummaryDto? = null,
  currentAccommodationTypeEntity: AccommodationTypeEntity? = null,
  nextAccommodations: List<AccommodationSummaryDto> = emptyList(),
  cas1Application: Cas1ApplicationDomainData? = null,
  cas2Application: Cas2Application? = null,
  cas3Application: Cas3Application? = null,
  dutyToRefer: DutyToReferDto? = null,
  commissionedRehabilitativeServices: CommissionedRehabilitativeServices? = null,
  sexualOffenceRegistrations: SexualOffenceRegistrations? = null,
) = DomainData(
  crn = crn,
  tierScore = tierScore,
  sex = sex,
  dob = dob,
  roshLevel = roshLevel,
  currentAccommodation = currentAccommodation,
  currentAccommodationTypeEntity = currentAccommodationTypeEntity,
  nextAccommodations = nextAccommodations,
  cas1Application = cas1Application,
  cas2Application = cas2Application,
  cas3Application = cas3Application,
  dutyToRefer = dutyToRefer,
  commissionedRehabilitativeServices = commissionedRehabilitativeServices,
  sexualOffenceRegistrations = sexualOffenceRegistrations,
)

fun buildCas1ApplicationDomainData(
  application: Cas1ApplicationV2 = buildCas1ApplicationV2(),
  currentOutcome: Cas1ExternalRequestForPlacementOutcome?,
  historyExcludingCurrentOutcome: List<Cas1ExternalRequestForPlacementOutcome> = emptyList(),
) = Cas1ApplicationDomainData(
  application = application,
  currentOutcome = currentOutcome,
  historyExcludingCurrentOutcome = historyExcludingCurrentOutcome,
)
