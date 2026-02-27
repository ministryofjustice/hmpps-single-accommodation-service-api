package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetailCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.ProposedAccommodationMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate
import java.time.Instant
import java.util.UUID

@Service
class ProposedAccommodationApplicationService(
  private val jsonMapper: JsonMapper,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val outboxEventRepository: OutboxEventRepository,
  private val userService: UserService,
) {
  @Transactional
  fun createProposedAccommodation(crn: String, accommodationDetailCommand: AccommodationDetailCommand): AccommodationDetail {
    val user = userService.getUserForRequest()
    val aggregate = ProposedAccommodationAggregate.hydrateNew(crn)
    aggregate.updateProposedAccommodation(
      newName = accommodationDetailCommand.name,
      newArrangementType = accommodationDetailCommand.arrangementType,
      newArrangementSubType = accommodationDetailCommand.arrangementSubType,
      newArrangementSubTypeDescription = accommodationDetailCommand.arrangementSubTypeDescription,
      newSettledType = accommodationDetailCommand.settledType,
      newVerificationStatus = accommodationDetailCommand.verificationStatus,
      newNextAccommodationStatus = accommodationDetailCommand.nextAccommodationStatus,
      newAddress = accommodationDetailCommand.address,
      newOffenderReleaseType = accommodationDetailCommand.offenderReleaseType,
      newStartDate = accommodationDetailCommand.startDate,
      newEndDate = accommodationDetailCommand.endDate,
    )
    val persistedRecord = proposedAccommodationRepository.save(
      ProposedAccommodationMapper.toEntity(aggregate.snapshot()),
    )
    pullEventAndPersistToOutbox(aggregate)
    return ProposedAccommodationMapper.toDto(
      snapshot = aggregate.snapshot(),
      createdBy = user.name,
      createdAt = persistedRecord.createdAt!!,
    )
  }

  @Transactional
  fun updateProposedAccommodation(crn: String, id: UUID, accommodationDetailCommand: AccommodationDetailCommand): AccommodationDetail {
    userService.getUserForRequest()
    val entity = proposedAccommodationRepository.findByIdAndCrn(id, crn)
      ?: throw NotFoundException("Proposed Accommodation not found for id: $id and crn: $crn")
    val aggregate = ProposedAccommodationMapper.toAggregate(entity)
    aggregate.updateProposedAccommodation(
      newName = accommodationDetailCommand.name,
      newArrangementType = accommodationDetailCommand.arrangementType,
      newArrangementSubType = accommodationDetailCommand.arrangementSubType,
      newArrangementSubTypeDescription = accommodationDetailCommand.arrangementSubTypeDescription,
      newSettledType = accommodationDetailCommand.settledType,
      newVerificationStatus = accommodationDetailCommand.verificationStatus,
      newNextAccommodationStatus = accommodationDetailCommand.nextAccommodationStatus,
      newAddress = accommodationDetailCommand.address,
      newOffenderReleaseType = accommodationDetailCommand.offenderReleaseType,
      newStartDate = accommodationDetailCommand.startDate,
      newEndDate = accommodationDetailCommand.endDate,
    )
    ProposedAccommodationMapper.applyToEntity(aggregate.snapshot(), entity)
    val updatedRecord = proposedAccommodationRepository.save(entity)
    pullEventAndPersistToOutbox(aggregate)

    val createdByUser = userService.findUserByUserId(updatedRecord.createdByUserId!!)!!
    return ProposedAccommodationMapper.toDto(
      snapshot = aggregate.snapshot(),
      createdBy = createdByUser.name,
      createdAt = updatedRecord.createdAt!!,
    )
  }

  private fun pullEventAndPersistToOutbox(aggregate: ProposedAccommodationAggregate) = aggregate.pullDomainEvents().forEach { event ->
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
}
