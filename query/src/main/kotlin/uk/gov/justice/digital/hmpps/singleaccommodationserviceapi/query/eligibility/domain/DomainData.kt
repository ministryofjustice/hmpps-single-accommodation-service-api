package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity

data class DomainData(
  val crn: String,
  val tierScore: String?,
  val sex: SexCode?,
  val currentAccommodation: AccommodationSummaryDto?,
  val currentAccommodationTypeEntity: AccommodationTypeEntity?,
  val nextAccommodation: AccommodationSummaryDto?,
  val cas1Application: Cas1Application?,
  val cas3Application: Cas3Application?,
  val dutyToRefer: DutyToReferDto?,
  val commissionedRehabilitativeServices: CommissionedRehabilitativeServices?,
) {
  constructor(
    crn: String,
    cpr: CorePersonRecord?,
    tier: Tier?,
    currentAccommodation: AccommodationSummaryDto?,
    nextAccommodation: AccommodationSummaryDto?,
    cas1Application: Cas1Application?,
    cas3Application: Cas3Application?,
    dutyToRefer: DutyToReferDto?,
    commissionedRehabilitativeServices: CommissionedRehabilitativeServices?,
    accommodationTypes: List<AccommodationTypeEntity>,
  ) : this(
    crn = crn,
    tierScore = tier?.tierScore,
    sex = cpr?.sex?.code,
    currentAccommodation = currentAccommodation,
    currentAccommodationTypeEntity = accommodationTypes.find { it.code == currentAccommodation?.type?.code },
    nextAccommodation = nextAccommodation,
    cas1Application = cas1Application,
    cas3Application = cas3Application,
    dutyToRefer = dutyToRefer,
    commissionedRehabilitativeServices = commissionedRehabilitativeServices,
  )

  constructor(
    crn: String,
    sexCode: SexCode?,
    caseEntity: CaseEntity?,
    dutyToRefer: DutyToReferDto?,
  ) : this(
    crn = crn,
    tierScore = caseEntity?.tierScore,
    sex = sexCode,
    currentAccommodation = null,
    currentAccommodationTypeEntity = null,
    nextAccommodation = null,
    cas1Application = if (caseEntity?.cas1ApplicationId != null && caseEntity.cas1ApplicationApplicationStatus != null) {
      Cas1Application(
        id = caseEntity.cas1ApplicationId!!,
        applicationStatus = caseEntity.cas1ApplicationApplicationStatus!!,
        requestForPlacementStatus = caseEntity.cas1ApplicationRequestForPlacementStatus,
        placementStatus = caseEntity.cas1ApplicationPlacementStatus,
        premises = null,
      )
    } else {
      null
    },
    cas3Application = null,
    dutyToRefer = dutyToRefer,
    commissionedRehabilitativeServices = null,
  )
}
