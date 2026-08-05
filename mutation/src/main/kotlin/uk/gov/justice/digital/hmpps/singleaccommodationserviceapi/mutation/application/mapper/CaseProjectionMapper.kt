package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseMutationOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate

object CaseProjectionMapper {

  fun create(
    projection: CaseMutationOrchestrationDto,
    crn: String,
    prisonNumber: String?,
  ): CaseEntity {
    val aggregate = CaseAggregate.hydrateNew()
    aggregate.applyProjection(projection)
    return CaseMapper.create(aggregate.snapshot(), crn, prisonNumber)
  }

  fun merge(
    entity: CaseEntity,
    projection: CaseMutationOrchestrationDto,
  ): CaseEntity {
    val aggregate = CaseMapper.toAggregate(entity)
    aggregate.applyProjection(projection)
    return CaseMapper.merge(entity, aggregate.snapshot())
  }

  private fun CaseAggregate.applyProjection(projection: CaseMutationOrchestrationDto) {
    this.upsertCase(
      tierScore = projection.tier?.tierScore,
      cas1ApplicationId = projection.cas1Application?.id,
      cas1ApplicationApplicationStatus = projection.cas1Application?.applicationStatus,
      cas1ApplicationRequestForPlacementStatus = projection.cas1Application?.requestForPlacementStatus,
      cas1ApplicationPlacementStatus = projection.cas1Application?.placementStatus,
    )
  }
}
