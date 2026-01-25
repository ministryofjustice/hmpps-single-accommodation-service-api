package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CreateAccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.ProposedAccommodationMapper
import java.time.Instant
import java.util.UUID

@Service
class ProposedAccommodationApplicationService(
  private val objectMapper: ObjectMapper,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val outboxEventRepository: OutboxEventRepository,
) {
  @Transactional
  fun createProposedAccommodation(crn: String, request: CreateAccommodationDetail): AccommodationDetail {
    val aggregate = ProposedAccommodationAggregate.hydrateNew(
      crn = crn
    )

    aggregate.createProposedAccommodation(
      newName = request.name,
      newArrangementType = request.arrangementType,
      newArrangementSubType = request.arrangementSubType,
      newArrangementSubTypeDescription = request.arrangementSubTypeDescription,
      newSettledType = request.settledType,
      newStatus = request.status!!,
      newAddress = request.address,
      newOffenderReleaseType = request.offenderReleaseType,
      newStartDate = request.startDate,
      newEndDate = request.endDate,
    )

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
          payload = objectMapper.writeValueAsString(event),
          createdAt = Instant.now(),
          processedStatus = ProcessedStatus.PENDING,
          processedAt = null,
        ),
      )
    }

    return ProposedAccommodationMapper.toDto(aggregate.snapshot())
  }
}
