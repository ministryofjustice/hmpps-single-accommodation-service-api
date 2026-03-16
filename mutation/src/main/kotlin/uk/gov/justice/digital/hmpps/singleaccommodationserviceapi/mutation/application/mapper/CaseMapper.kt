package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate.CaseSnapshot

object CaseMapper {

  // TODO deal with double bangs
  fun toEntity(snapshot: CaseSnapshot) = CaseEntity(
    id = snapshot.id,
    crn = snapshot.crn,
    tier = snapshot.tier?.name?.let { TierScore.valueOf(it) },
    currentAccommodationArrangementType = snapshot.currentAccommodationArrangementType,
    nextAccommodationId = snapshot.nextAccommodationId,
    crsStatus = snapshot.crsStatus,
    dtrStatus = snapshot.dtrStatus,
    cas3ApplicationPlacementStatus = snapshot.cas3ApplicationPlacementStatus,
    cas3ApplicationApplicationStatus = snapshot.cas3ApplicationApplicationStatus,
    cas3ApplicationId = snapshot.cas3ApplicationId,
    cas2HdcApplicationId = snapshot.cas2HdcApplicationId,
    cas2PrisonBailApplicationId = snapshot.cas2PrisonBailApplicationId,
    cas2CourtBailApplicationId = snapshot.cas2CourtBailApplicationId,
    cas1ApplicationPlacementStatus = snapshot.cas1ApplicationPlacementStatus,
    cas1ApplicationApplicationStatus = snapshot.cas1ApplicationApplicationStatus,
    cas1ApplicationId = snapshot.cas1ApplicationId,
  )

  fun toAggregate(entity: CaseEntity): CaseAggregate = CaseAggregate.hydrate(
    id = entity.id,
    crn = entity.crn,
    tier = entity.tier,
  )
}
