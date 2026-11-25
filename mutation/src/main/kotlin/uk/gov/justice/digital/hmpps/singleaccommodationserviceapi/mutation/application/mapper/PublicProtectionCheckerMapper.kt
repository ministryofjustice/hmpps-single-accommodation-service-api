package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.PublicProtectionCheckerEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.PublicProtectionCheckerAggregate.PublicProtectionCheckerSnapshot

object PublicProtectionCheckerMapper {

  fun toEntity(snapshot: PublicProtectionCheckerSnapshot) = PublicProtectionCheckerEntity(
    id = snapshot.id,
    sasCaseId = snapshot.caseId,
    supervisionRequired = snapshot.supervisionRequired,
    controlMeasuresSuitable = snapshot.controlMeasuresSuitable,
    practicalSupportSufficient = snapshot.practicalSupportSufficient,
  )
}
