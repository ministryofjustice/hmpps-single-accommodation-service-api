package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2CourtBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2HdcApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2PrisonBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus

fun buildActions(cas1Application: Cas1Application) = if (cas1Application.applicationStatus != Cas1ApplicationStatus.PLACEMENT_ALLOCATED ||
  cas1Application.placementStatus in setOf(
    Cas1PlacementStatus.CANCELLED,
    Cas1PlacementStatus.DEPARTED,
    Cas1PlacementStatus.NOT_ARRIVED,
  )
) {
  listOf("Create a placement request.")
} else {
  emptyList()
}

fun buildActions(cas2HdcApplication: Cas2HdcApplication) = emptyList<String>()

fun buildActions(cas2CourtBailApplication: Cas2CourtBailApplication) = emptyList<String>()

fun buildActions(cas2PrisonBailApplication: Cas2PrisonBailApplication) = emptyList<String>()
