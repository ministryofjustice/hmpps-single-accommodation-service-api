package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NoteCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ProposedAccommodationDetailCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ProposedAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationStatusEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationStatusRepository
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
  private val accommodationStatusRepository: AccommodationStatusRepository,
  private val outboxEventRepository: OutboxEventRepository,
  private val userService: UserService,
  private val caseRepository: CaseRepository,
) {
  @Transactional
  fun createProposedAccommodation(
    crn: String,
    currentAccommodation: AccommodationSummaryDto?,
    proposedAccommodationDetailCommand: ProposedAccommodationDetailCommand,
  ): ProposedAccommodationDto {
    val user = userService.authorizeAndRetrieveUser()
    val case = caseRepository.findByCrn(crn)
      .orThrowNotFound("crn" to crn)
    val accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(proposedAccommodationDetailCommand.accommodationTypeCode)
      .orThrowNotFound("code" to proposedAccommodationDetailCommand.accommodationTypeCode)
    val aggregate = ProposedAccommodationAggregate.hydrateNew(caseId = case.id, currentAccommodation = currentAccommodation)
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
    val aggregateSnapshot = aggregate.snapshot()
    val accommodationStatusEntity = aggregateSnapshot.accommodationStatus
      ?.let {
        accommodationStatusRepository.findByCodeAndActiveIsTrue(it.code)
          .orThrowNotFound("code" to it.code)
      }
    val persistedRecord = proposedAccommodationRepository.save(
      ProposedAccommodationMapper.toEntity(
        aggregateSnapshot,
        accommodationTypeEntity,
        accommodationStatusEntity,
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
  fun updateProposedAccommodation(
    id: UUID,
    crn: String,
    proposedAccommodationDetailCommand: ProposedAccommodationDetailCommand,
    currentAccommodation: AccommodationSummaryDto?,
  ): ProposedAccommodationDto {
    val proposedAccommodationEntity = proposedAccommodationRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val accommodationTypeEntity = accommodationTypeRepository.findByCodeAndActiveIsTrue(proposedAccommodationDetailCommand.accommodationTypeCode)
      .orThrowNotFound("code" to proposedAccommodationDetailCommand.accommodationTypeCode)
    val accommodationStatusEntity = proposedAccommodationEntity.accommodationStatusId
      ?.let {
        accommodationStatusRepository.findByIdOrNull(it)
          .orThrowNotFound("id" to it)
      }
    val aggregate = ProposedAccommodationMapper.toAggregate(
      proposedAccommodationEntity,
      accommodationTypeEntity,
      accommodationStatusEntity,
      currentAccommodation,
    )
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
    val updatedRecord = proposedAccommodationRepository.save(
      merge(
        snapshot = aggregate.snapshot(),
        proposedAccommodationEntity,
        accommodationTypeEntity,
        accommodationStatusEntity = getAccommodationStatusEntity(
          preUpdateAccommodationStatusEntity = accommodationStatusEntity,
          aggregate,
        ),
      ),
    )

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

  private fun getAccommodationStatusEntity(
    preUpdateAccommodationStatusEntity: AccommodationStatusEntity?,
    aggregate: ProposedAccommodationAggregate,
  ) = if (aggregate.snapshot().accommodationStatus?.code == preUpdateAccommodationStatusEntity?.code) {
    preUpdateAccommodationStatusEntity
  } else {
    aggregate.snapshot().accommodationStatus
      ?.let {
        accommodationStatusRepository.findByCodeAndActiveIsTrue(it.code)
          .orThrowNotFound("code" to it.code)
      }
  }

  @Transactional
  fun createProposedAccommodationNote(
    id: UUID,
    crn: String,
    noteCommand: NoteCommand,
    currentAccommodation: AccommodationSummaryDto?,
  ) {
    val proposedAccommodationEntity = proposedAccommodationRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val accommodationTypeEntity = accommodationTypeRepository.findByIdOrNull(proposedAccommodationEntity.accommodationTypeId)
      .orThrowNotFound("accommodationTypeId" to proposedAccommodationEntity.accommodationTypeId)
    val accommodationStatusEntity = proposedAccommodationEntity.accommodationStatusId
      ?.let {
        accommodationStatusRepository.findByIdOrNull(it)
          .orThrowNotFound("id" to it)
      }
    val aggregate = ProposedAccommodationMapper.toAggregate(
      proposedAccommodationEntity,
      accommodationTypeEntity,
      accommodationStatusEntity,
      currentAccommodation,
    )
    aggregate.addNote(note = noteCommand.note)
    val merged = merge(
      snapshot = aggregate.snapshot(),
      proposedAccommodationEntity,
      accommodationTypeEntity,
      accommodationStatusEntity = getAccommodationStatusEntity(
        preUpdateAccommodationStatusEntity = accommodationStatusEntity,
        aggregate,
      ),
    )
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
