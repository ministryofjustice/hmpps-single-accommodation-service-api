package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1SuitablePremisesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3SuitablePremisesDto
import java.time.LocalDate
import java.util.UUID

fun buildCas1Application(
  id: UUID = UUID.randomUUID(),
  applicationStatus: Cas1ApplicationStatus = Cas1ApplicationStatus.STARTED,
  placementStatus: Cas1PlacementStatus? = null,
  requestForPlacementStatus: Cas1RequestForPlacementStatus? = null,
  premises: Cas1SuitablePremisesDto? = null,
) = Cas1Application(
  id = id,
  applicationStatus = applicationStatus,
  placementStatus = placementStatus,
  requestForPlacementStatus = requestForPlacementStatus,
  premises = premises,
)

fun buildCas1SuitablePremisesDto(
  startDate: LocalDate? = LocalDate.now().plusDays(1),
  endDate: LocalDate? = LocalDate.now().plusDays(10),
  addressLine1: String = "123 Test Street",
  addressLine2: String? = "Test Village",
  town: String? = "Test Town",
  postcode: String = "AB1 2CD",
) = Cas1SuitablePremisesDto(
  startDate = startDate,
  endDate = endDate,
  addressLine1 = addressLine1,
  addressLine2 = addressLine2,
  town = town,
  postcode = postcode,
)

fun buildCas3Application(
  id: UUID = UUID.randomUUID(),
  applicationStatus: Cas3ApplicationStatus = Cas3ApplicationStatus.IN_PROGRESS,
  assessmentStatus: Cas3AssessmentStatus? = null,
  bookingStatus: Cas3BookingStatus? = null,
  premises: Cas3SuitablePremisesDto? = null,
) = Cas3Application(
  id = id,
  applicationStatus = applicationStatus,
  bookingStatus = bookingStatus,
  assessmentStatus = assessmentStatus,
  premises = premises,
)

fun buildCas3SuitablePremisesDto(
  startDate: LocalDate? = LocalDate.now().plusDays(1),
  endDate: LocalDate? = LocalDate.now().plusDays(10),
  addressLine1: String = "123 Test Street",
  addressLine2: String? = "Test Village",
  town: String? = "Test Town",
  postcode: String = "AB1 2CD",
  name: String? = "Test Premises",
) = Cas3SuitablePremisesDto(
  startDate = startDate,
  endDate = endDate,
  addressLine1 = addressLine1,
  addressLine2 = addressLine2,
  town = town,
  postcode = postcode,
  name = name,
)
