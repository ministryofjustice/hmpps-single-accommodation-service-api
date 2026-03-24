package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.Case

object PersonTransformer {
  fun toPersonDto(
    caseListItem: Case,
  ) = PersonDto(
    crn = caseListItem.crn,
    name = caseListItem.name,
    nomsNumber = caseListItem.nomsNumber,
    pncNumber = caseListItem.pncNumber,
    dateOfBirth = caseListItem.dateOfBirth,
    staff = caseListItem.staff,
    team = caseListItem.team,
    gender = caseListItem.gender,
    roshLevel = caseListItem.roshLevel,
    expectedReleaseDate = caseListItem.expectedReleaseDate,
    userExcluded = caseListItem.userExcluded,
    userRestricted = caseListItem.userRestricted,
    exclusionMessage = caseListItem.exclusionMessage,
    restrictionMessage = caseListItem.restrictionMessage,
  )
}
