package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock

import java.time.LocalDate
import java.time.LocalDateTime

val mockCrns = listOf(
  "X371199",
  "X968879",
  "X966926",
  "X969031",
)

val mockedLocalDate: LocalDate = LocalDate.of(1970, 1, 1)

val mockedLocalDateTime: LocalDateTime = LocalDateTime.parse("1970-01-01T00:00:00")

val mockPhotoUrl: String =
  "https://github.com/ministryofjustice/hmpps-single-accommodation-service-prototype/blob/main/app/assets/images/profile-placeholder-2.jpg"
