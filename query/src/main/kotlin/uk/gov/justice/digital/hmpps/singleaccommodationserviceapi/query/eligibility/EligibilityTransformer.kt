package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import kotlin.collections.orEmpty

  fun toEligibilityDto(
    crn: String,
    cas1: ServiceResult,
    cas2Hdc: ServiceResult? = null,
    cas2PrisonBail: ServiceResult? = null,
    cas2CourtBail: ServiceResult? = null,
    cas3: ServiceResult? = null,
  ) = EligibilityDto(
    crn = crn,
    cas1 = cas1,
    cas2Hdc = cas2Hdc,
    cas2PrisonBail = cas2PrisonBail,
    cas2CourtBail = cas2CourtBail,
    cas3 = cas3,
    caseActions = cas1.actions +
      cas2Hdc?.actions.orEmpty() +
      cas2CourtBail?.actions.orEmpty() +
      cas2PrisonBail?.actions.orEmpty() +
      cas3?.actions.orEmpty(),
    caseStatus = listOf(
      getCaseStatus(cas1.serviceStatus),
      getCaseStatusOrDefault(cas2Hdc),
      getCaseStatusOrDefault(cas2CourtBail),
      getCaseStatusOrDefault(cas2PrisonBail),
      getCaseStatusOrDefault(cas3),
    ).maxWith(compareBy<CaseStatus> { it.caseStatusOrder }),
  )

  private fun getCaseStatusOrDefault(serviceResult: ServiceResult?) = serviceResult?.let { getCaseStatus(serviceResult.serviceStatus) } ?: CaseStatus.NO_ACTION_NEEDED

  private fun getCaseStatus(serviceStatus: ServiceStatus) = when (serviceStatus) {
    ServiceStatus.UPCOMING -> CaseStatus.ACTION_UPCOMING
    ServiceStatus.NOT_ELIGIBLE, ServiceStatus.ARRIVED, ServiceStatus.UPCOMING_PLACEMENT -> CaseStatus.NO_ACTION_NEEDED
    else -> CaseStatus.ACTION_NEEDED
  }

