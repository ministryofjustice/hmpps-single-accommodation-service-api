package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.mock

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

val mockedLocalDate: LocalDate = LocalDate.of(1970, 1, 1)
val mockedOffsetDateTime: OffsetDateTime = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
val mockedZonedDateTime = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
