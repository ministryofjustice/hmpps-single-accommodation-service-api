package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.PublicProtectionCheckerEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.TestData
import java.util.UUID

@TestData
fun buildPublicProtectionCheckerEntity(
  id: UUID = UUID.randomUUID(),
  sasCaseId: UUID = UUID.randomUUID(),
  supervisionRequired: Boolean = false,
  controlMeasuresSuitable: Boolean = false,
  practicalSupportSufficient: Boolean = false,
) = PublicProtectionCheckerEntity(
  id = id,
  sasCaseId = sasCaseId,
  supervisionRequired = supervisionRequired,
  controlMeasuresSuitable = controlMeasuresSuitable,
  practicalSupportSufficient = practicalSupportSufficient,
)
