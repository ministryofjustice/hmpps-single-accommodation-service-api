package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseIdentifierEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun buildCaseEntity(
  id: UUID = UUID.randomUUID(),
  tierScore: String? = "A1",
  hasSyncedCprProposedAccommodation: Boolean = false,
  firstName: String? = "First",
  lastName: String? = "Last",
  dateOfBirth: LocalDate? = LocalDate.of(2000, 12, 3),
  roshLevelCode: String? = null,
  customise: (CaseEntity.() -> Unit)? = null,
) = CaseEntity(
  id = id,
  tierScore = tierScore,
  hasSyncedCprProposedAccommodation = hasSyncedCprProposedAccommodation,
  firstName = firstName,
  lastName = lastName,
  dateOfBirth = dateOfBirth,
  roshLevelCode = roshLevelCode,
).also { case ->

  if (customise != null) {
    case.customise()
  } else {
    case.withCrn(UUID.randomUUID().toString())
  }
}

fun CaseEntity.withIdentifier(
  identifier: String,
  type: IdentifierType,
) {
  caseIdentifiers.add(
    buildCaseIdentifier(
      identifier = identifier,
      identifierType = type,
      caseEntity = this,
    ),
  )
}

fun CaseEntity.withCrn(crn: String) = withIdentifier(crn, IdentifierType.CRN)

fun CaseEntity.withPrisonNumber(prisonNumber: String) = this.withIdentifier(prisonNumber, IdentifierType.PRISON_NUMBER)

fun buildCaseIdentifier(
  id: UUID = UUID.randomUUID(),
  caseEntity: CaseEntity,
  identifier: String = "DEFAULT",
  identifierType: IdentifierType = IdentifierType.CRN,
  createdAt: Instant = Instant.now(),
) = CaseIdentifierEntity(
  id = id,
  caseEntity = caseEntity,
  identifier = identifier,
  identifierType = identifierType,
  createdAt = createdAt,
)
