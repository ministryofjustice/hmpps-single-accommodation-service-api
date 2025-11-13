package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mocks

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.model.Case

fun mockUserCases(): List<Case> = listOf(
  Case(
    crn = "M127638",
    name = "Kwame Mensah",
  ),
  Case(
    crn = "Y785304",
    name = "Aaron Buckley",
  ),
  Case(
    crn = "X100019",
    name = "Steve Walker",
  ),
  Case(
    crn = "X100004",
    name = "Nicholas Clark",
  ),
  Case(
    crn = "G772051",
    name = "Rhys Davies",
  ),
  Case(
    crn = "X100024",
    name = "Tariq Garcia",
  ),
  Case(
    crn = "X100009",
    name = "Igor Cheung",
  ),
  Case(
    crn = "X100030",
    name = "Henry Walker",
  ),
  Case(
    crn = "X100034",
    name = "Edward Brown",
  ),
)
