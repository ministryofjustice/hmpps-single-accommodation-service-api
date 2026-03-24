package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate.CaseSnapshot

object CaseMapper {

  fun toEntity(snapshot: CaseSnapshot) = CaseEntity(
    id = snapshot.id,
    crn = snapshot.crn,
    tier = snapshot.tier?.let { TierScore.valueOf(snapshot.tier.name) },
    cas1ApplicationId = snapshot.cas1ApplicationId,
    cas1ApplicationApplicationStatus = snapshot.cas1ApplicationApplicationStatus,
    cas1ApplicationRequestForPlacementStatus = snapshot.cas1ApplicationRequestForPlacementStatus,
    cas1ApplicationPlacementStatus = snapshot.cas1ApplicationPlacementStatus,
  )

  fun toAggregate(entity: CaseEntity): CaseAggregate = CaseAggregate.hydrate(
    id = entity.id,
    crn = entity.crn,
    tier = entity.tier,
    cas1ApplicationId = entity.cas1ApplicationId,
    cas1ApplicationApplicationStatus = entity.cas1ApplicationApplicationStatus,
    cas1ApplicationRequestForPlacementStatus = entity.cas1ApplicationRequestForPlacementStatus,
    cas1ApplicationPlacementStatus = entity.cas1ApplicationPlacementStatus,
  )
}
