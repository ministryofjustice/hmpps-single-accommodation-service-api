package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NoteCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ProposedAccommodationDetailCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ProposedAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.ProposedAccommodationMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.ProposedAccommodationMapper.merge
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate
import java.time.Instant
import java.util.UUID

@Service
class ProposedAccommodationApplicationService(
  private val jsonMapper: JsonMapper,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val accommodationTypeRepository: AccommodationTypeRepository,
  private val outboxEventRepository: OutboxEventRepository,
  private val userService: UserService,
  private val caseRepository: CaseRepository,
) {
  @Transactional
  fun createProposedAccommodation(crn: String, proposedAccommodationDetailCommand: ProposedAccommodationDetailCommand): ProposedAccommodationDto {
    val user = userService.authorizeAndRetrieveUser()
    val case = caseRepository.findByCrn(crn)
      .orThrowNotFound("crn" to crn)
    val accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(proposedAccommodationDetailCommand.accommodationTypeCode)
      .orThrowNotFound("code" to proposedAccommodationDetailCommand.accommodationTypeCode)
    val aggregate = ProposedAccommodationAggregate.hydrateNew(case.id)
    aggregate.updateProposedAccommodation(
      newName = proposedAccommodationDetailCommand.name,
      newAccommodationType = AccommodationTypeDto(
        code = accommodationTypeEntity.code,
        description = accommodationTypeEntity.name,
      ),
      newVerificationStatus = proposedAccommodationDetailCommand.verificationStatus,
      newNextAccommodationStatus = proposedAccommodationDetailCommand.nextAccommodationStatus,
      newAddress = proposedAccommodationDetailCommand.address,
      newStartDate = proposedAccommodationDetailCommand.startDate,
      newEndDate = proposedAccommodationDetailCommand.endDate,
    )
    val persistedRecord = proposedAccommodationRepository.save(
      ProposedAccommodationMapper.toEntity(
        aggregate.snapshot(),
        accommodationTypeEntity,
      ),
    )
    pullEventAndPersistToOutbox(aggregate)
    return ProposedAccommodationMapper.toDto(
      snapshot = aggregate.snapshot(),
      crn = crn,
      createdBy = user.name,
      createdAt = persistedRecord.createdAt!!,
    )
  }

  @Transactional
  fun updateProposedAccommodation(crn: String, id: UUID, proposedAccommodationDetailCommand: ProposedAccommodationDetailCommand): ProposedAccommodationDto {
    val proposedAccommodationEntity = proposedAccommodationRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(proposedAccommodationDetailCommand.accommodationTypeCode)
      .orThrowNotFound("code" to proposedAccommodationDetailCommand.accommodationTypeCode)
    val aggregate = ProposedAccommodationMapper.toAggregate(proposedAccommodationEntity, accommodationTypeEntity)
    aggregate.updateProposedAccommodation(
      newName = proposedAccommodationDetailCommand.name,
      newAccommodationType = AccommodationTypeDto(
        code = accommodationTypeEntity.code,
        description = accommodationTypeEntity.name,
      ),
      newVerificationStatus = proposedAccommodationDetailCommand.verificationStatus,
      newNextAccommodationStatus = proposedAccommodationDetailCommand.nextAccommodationStatus,
      newAddress = proposedAccommodationDetailCommand.address,
      newStartDate = proposedAccommodationDetailCommand.startDate,
      newEndDate = proposedAccommodationDetailCommand.endDate,
    )
    val updatedRecord = proposedAccommodationRepository.save(merge(aggregate.snapshot(), proposedAccommodationEntity, accommodationTypeEntity))
    pullEventAndPersistToOutbox(aggregate)

    val createdByUser = userService.findUserByUserId(updatedRecord.createdByUserId!!)
      .orThrowNotFound("id" to updatedRecord.createdByUserId!!)
    return ProposedAccommodationMapper.toDto(
      snapshot = aggregate.snapshot(),
      crn = crn,
      createdBy = createdByUser.name,
      createdAt = updatedRecord.createdAt!!,
    )
  }

  @Transactional
  fun createProposedAccommodationNote(crn: String, id: UUID, noteCommand: NoteCommand) {
    val proposedAccommodationEntity = proposedAccommodationRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val accommodationTypeEntity = accommodationTypeRepository.findByIdOrNull(proposedAccommodationEntity.accommodationTypeId)
      .orThrowNotFound("accommodationTypeId" to proposedAccommodationEntity.accommodationTypeId)
    val aggregate = ProposedAccommodationMapper.toAggregate(proposedAccommodationEntity, accommodationTypeEntity)
    aggregate.addNote(note = noteCommand.note)
    val merged = merge(aggregate.snapshot(), proposedAccommodationEntity, accommodationTypeEntity)
    proposedAccommodationRepository.save(merged)
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
