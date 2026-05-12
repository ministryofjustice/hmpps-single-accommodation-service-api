package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.CommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.CrsStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import java.time.LocalDate

fun buildDomainData(
  crn: String = "CR12345N",
  tierScore: TierScore? = null,
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

fun buildCommissionedRehabilitativeServices(
  submissionDate: LocalDate = LocalDate.now(),
  status: CrsStatus = CrsStatus.COMPLETED,
) = CommissionedRehabilitativeServices(
  status = status,
  submissionDate = submissionDate,
)
