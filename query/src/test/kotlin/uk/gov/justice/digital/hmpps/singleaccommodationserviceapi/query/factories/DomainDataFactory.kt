package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData

fun buildDomainData(
  crn: String = "CR12345N",
  tierScore: String? = null,
  sex: SexCode? = null,
  currentAccommodation: AccommodationSummaryDto? = null,
  currentAccommodationTypeEntity: AccommodationTypeEntity? = null,
  nextAccommodation: AccommodationSummaryDto? = null,
  cas1Application: Cas1Application? = null,
  cas3Application: Cas3Application? = null,
  dutyToRefer: DutyToReferDto? = null,
  commissionedRehabilitativeServices: CommissionedRehabilitativeServices? = null,
) = DomainData(
  crn = crn,
  tierScore = tierScore,
  sex = sex,
  currentAccommodation = currentAccommodation,
  currentAccommodationTypeEntity = currentAccommodationTypeEntity,
  nextAccommodation = nextAccommodation,
  cas1Application = cas1Application,
  cas3Application = cas3Application,
  dutyToRefer = dutyToRefer,
  commissionedRehabilitativeServices = commissionedRehabilitativeServices,
)
