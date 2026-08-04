package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseMutationOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate

object CaseProjectionMapper {

  fun create(
    projection: CaseMutationOrchestrationDto,
    identifiers: Map<String, IdentifierType>,
  ): CaseEntity {
    val aggregate = CaseAggregate.hydrateNew()
    aggregate.apply(projection)
    return CaseMapper.create(aggregate.snapshot(), identifiers)
  }

  fun merge(
    entity: CaseEntity,
    projection: CaseMutationOrchestrationDto,
  ): CaseEntity {
    val aggregate = CaseMapper.toAggregate(entity)
    aggregate.apply(projection)
    return CaseMapper.merge(entity, aggregate.snapshot())
  }

  private fun CaseAggregate.apply(projection: CaseMutationOrchestrationDto) {
    upsertCase(
      tierScore = projection.tier?.tierScore,
      cas1ApplicationId = projection.cas1Application?.id,
      cas1ApplicationApplicationStatus = projection.cas1Application?.applicationStatus,
      cas1ApplicationRequestForPlacementStatus = projection.cas1Application?.requestForPlacementStatus,
      cas1ApplicationPlacementStatus = projection.cas1Application?.placementStatus,
    )
  }
}
