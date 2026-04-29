package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas3BookingStatus
import java.util.UUID

fun buildCas3ApplicationDto(
  id: UUID = UUID.randomUUID(),
  applicationStatus: Cas3ApplicationStatus = Cas3ApplicationStatus.IN_PROGRESS,
  assessmentStatus: Cas3AssessmentStatus? = null,
  bookingStatus: Cas3BookingStatus? = null,
) = Cas3ApplicationDto(
  id = id,
  applicationStatus = applicationStatus,
  bookingStatus = bookingStatus,
  assessmentStatus = assessmentStatus,
)
