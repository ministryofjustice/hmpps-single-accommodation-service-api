package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import java.time.LocalDate

data class DomainData(
  val crn: String,
  val tierScore: TierScore?,
  val sex: SexCode?,
  val currentAccommodation: AccommodationSummaryDto?,
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
  ) : this(
    crn = crn,
    tierScore = tier?.tierScore,
    sex = cpr?.sex?.code,
    currentAccommodation = currentAccommodation,
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
    nextAccommodation = null,
    cas1Application = if (caseEntity?.cas1ApplicationId != null && caseEntity.cas1ApplicationApplicationStatus != null) {
      Cas1Application(
        id = caseEntity.cas1ApplicationId!!,
        applicationStatus = caseEntity.cas1ApplicationApplicationStatus!!,
        requestForPlacementStatus = caseEntity.cas1ApplicationRequestForPlacementStatus,
        placementStatus = caseEntity.cas1ApplicationPlacementStatus,
      )
    } else {
      null
    },
    cas3Application = null,
    dutyToRefer = dutyToRefer,
    commissionedRehabilitativeServices = null,
  )
}

data class CommissionedRehabilitativeServices(
  val status: CrsStatus,
  val submissionDate: LocalDate,
)

enum class CrsStatus {
  NSI_REFERRAL,
  IN_PROGRESS,
  NSI_COMMENCED,
  APPOINTMENT,
  ACTION_PLAN_SUBMITTED,
  ACTION_PLAN_APPROVED,
  END_OF_SERVICE_REPORT,
  COMPLETED,
  NSI_TERMINATED,
}
