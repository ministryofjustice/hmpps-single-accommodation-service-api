package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Case

object PersonTransformer {
  fun toPersonDto(
    case: Case,
  ) = PersonDto(
    crn = case.crn,
    name = case.name.fullName,
    nomsNumber = case.nomsNumber,
    pncNumber = case.pncNumber,
    dateOfBirth = case.dateOfBirth,
    staff = case.staff,
    gender = case.gender,
    roshLevel = case.roshLevel?.code?.let { RiskLevel.findByCode(it) },
  )
}
