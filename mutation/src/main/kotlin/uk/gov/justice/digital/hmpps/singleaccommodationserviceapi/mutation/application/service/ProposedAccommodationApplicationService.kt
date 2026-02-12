package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CreateAccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpdateAccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.ProposedAccommodationMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NotFoundException
import java.time.Instant
import java.util.UUID

@Service
class ProposedAccommodationApplicationService(
  private val jsonMapper: JsonMapper,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val outboxEventRepository: OutboxEventRepository,
) {
  @Transactional
  fun createProposedAccommodation(crn: String, request: CreateAccommodationDetail): AccommodationDetail {
    val aggregate = ProposedAccommodationAggregate.hydrateNew(
      crn = crn,
    )

    aggregate.updateProposedAccommodation(
      newName = request.name,
      newArrangementType = request.arrangementType,
      newArrangementSubType = request.arrangementSubType,
      newArrangementSubTypeDescription = request.arrangementSubTypeDescription,
      newSettledType = request.settledType,
      newVerificationStatus = request.verificationStatus,
      newNextAccommodationStatus = request.nextAccommodationStatus,
      newAddress = request.address,
      newOffenderReleaseType = request.offenderReleaseType,
      newStartDate = request.startDate,
      newEndDate = request.endDate,
    )

    return saveAndPublish(aggregate)
  }

  @Transactional
  fun updateProposedAccommodation(crn: String, id: UUID, request: UpdateAccommodationDetail): AccommodationDetail {
    val entity = proposedAccommodationRepository.findByIdAndCrn(id, crn)
      ?: throw NotFoundException("Proposed Accommodation not found for id: $id and crn: $crn")

    val aggregate = ProposedAccommodationMapper.toAggregate(entity)

    aggregate.updateProposedAccommodation(
      newName = request.name,
      newArrangementType = request.arrangementType,
      newArrangementSubType = request.arrangementSubType,
      newArrangementSubTypeDescription = request.arrangementSubTypeDescription,
      newSettledType = request.settledType,
      newVerificationStatus = request.verificationStatus,
      newNextAccommodationStatus = request.nextAccommodationStatus,
      newAddress = request.address,
      newOffenderReleaseType = request.offenderReleaseType,
      newStartDate = request.startDate,
      newEndDate = request.endDate,
    )

    return saveAndPublish(aggregate)
  }

  private fun saveAndPublish(aggregate: ProposedAccommodationAggregate): AccommodationDetail {
    proposedAccommodationRepository.save(
      ProposedAccommodationMapper.toEntity(aggregate.snapshot()),
    )

    aggregate.pullDomainEvents().forEach { event ->
      outboxEventRepository.save(
        OutboxEventEntity(
          id = UUID.randomUUID(),
          aggregateId = event.aggregateId,
          aggregateType = "ProposedAccommodation",
          domainEventType = event.type.name,
          payload = jsonMapper.writeValueAsString(event),
          createdAt = Instant.now(),
          processedStatus = ProcessedStatus.PENDING,
          processedAt = null,
        ),
      )
    }

    return ProposedAccommodationMapper.toDto(aggregate.snapshot())
  }
}
