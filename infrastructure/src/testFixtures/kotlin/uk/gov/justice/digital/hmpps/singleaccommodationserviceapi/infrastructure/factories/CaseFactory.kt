package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseIdentifierEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import java.time.Instant
import java.util.UUID

fun buildCaseEntity(
  id: UUID = UUID.randomUUID(),
  tier: TierScore? = TierScore.A1,
  cas1ApplicationId: UUID? = null,
  cas1ApplicationApplicationStatus: Cas1ApplicationStatus? = null,
  cas1ApplicationRequestForPlacementStatus: Cas1RequestForPlacementStatus? = null,
  cas1ApplicationPlacementStatus: Cas1PlacementStatus? = null,
  customise: (CaseEntity.() -> Unit)? = {},
) = CaseEntity(
  id = id,
  tierScore = tier,
  cas1ApplicationId = cas1ApplicationId,
  cas1ApplicationApplicationStatus = cas1ApplicationApplicationStatus,
  cas1ApplicationRequestForPlacementStatus = cas1ApplicationRequestForPlacementStatus,
  cas1ApplicationPlacementStatus = cas1ApplicationPlacementStatus,
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
