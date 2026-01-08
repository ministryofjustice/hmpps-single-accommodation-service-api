package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2CourtBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2HdcApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2PrisonBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus

fun toServiceStatus(cas1Application: Cas1Application) = toServiceStatus(cas1Application.applicationStatus)
  ?: cas1Application.placementStatus?.let { toServiceStatus(cas1PlacementStatus = it) }
  ?: error("Null Placement Status")

fun toServiceStatus(cas2HdcApplication: Cas2HdcApplication) = ServiceStatus.NOT_STARTED

fun toServiceStatus(cas2CourtBailApplication: Cas2CourtBailApplication) = ServiceStatus.NOT_STARTED

fun toServiceStatus(cas2PrisonBailApplication: Cas2PrisonBailApplication) = ServiceStatus.NOT_STARTED

private fun toServiceStatus(cas1ApplicationStatus: Cas1ApplicationStatus) = when (cas1ApplicationStatus) {
  Cas1ApplicationStatus.PLACEMENT_ALLOCATED -> null
  Cas1ApplicationStatus.AWAITING_ASSESSMENT -> ServiceStatus.AWAITING_ASSESSMENT
  Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT -> ServiceStatus.UNALLOCATED_ASSESSMENT
  Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS -> ServiceStatus.ASSESSMENT_IN_PROGRESS
  Cas1ApplicationStatus.AWAITING_PLACEMENT -> ServiceStatus.AWAITING_PLACEMENT
  Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION -> ServiceStatus.REQUEST_FOR_FURTHER_INFORMATION
  Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST -> ServiceStatus.PENDING_PLACEMENT_REQUEST
}

private fun toServiceStatus(cas1PlacementStatus: Cas1PlacementStatus) = when (cas1PlacementStatus) {
  Cas1PlacementStatus.UPCOMING -> ServiceStatus.UPCOMING_PLACEMENT
  Cas1PlacementStatus.ARRIVED -> ServiceStatus.ARRIVED
  Cas1PlacementStatus.DEPARTED -> ServiceStatus.DEPARTED
  Cas1PlacementStatus.NOT_ARRIVED -> ServiceStatus.NOT_ARRIVED
  Cas1PlacementStatus.CANCELLED -> ServiceStatus.CANCELLED
}
