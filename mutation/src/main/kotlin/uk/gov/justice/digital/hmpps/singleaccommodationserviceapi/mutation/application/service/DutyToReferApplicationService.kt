package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NoteCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.DutyToReferMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.DutyToReferMapper.merge
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.DutyToReferAggregate
import java.time.Instant
import java.util.UUID

@Service
class DutyToReferApplicationService(
  private val jsonMapper: JsonMapper,
  private val dutyToReferRepository: DutyToReferRepository,
  private val localAuthorityAreaRepository: LocalAuthorityAreaRepository,
  private val outboxEventRepository: OutboxEventRepository,
  private val userService: UserService,
  private val caseRepository: CaseRepository,
) {
  @Transactional
  fun createDutyToRefer(crn: String, command: DtrCommand): DutyToReferDto {
    val user = userService.authorizeAndRetrieveUser()
    val case = caseRepository.findByCrn(crn).orThrowNotFound("crn" to crn)
    val aggregate = DutyToReferAggregate.hydrateNew(caseId = case.id)
    aggregate.updateDutyToRefer(
      localAuthorityAreaId = command.localAuthorityAreaId,
      submissionDate = command.submissionDate,
      referenceNumber = command.referenceNumber,
      status = command.status,
    )
    val persistedRecord = dutyToReferRepository.save(
      DutyToReferMapper.toEntity(aggregate.snapshot()),
    )
    pullEventAndPersistToOutbox(aggregate)
    val localAuthorityArea = localAuthorityAreaRepository.findByIdOrNull(persistedRecord.localAuthorityAreaId)!!
    return DutyToReferMapper.toDto(
      snapshot = aggregate.snapshot(),
      crn = crn,
      createdBy = user.name,
      createdAt = persistedRecord.createdAt!!,
      localAuthorityAreaName = localAuthorityArea.name,
    )
  }

  @Transactional
  fun updateDutyToRefer(crn: String, id: UUID, command: DtrCommand): DutyToReferDto {
    val dtr = dutyToReferRepository.findByIdAndCrn(id, crn)
      .orThrowNotFound("id" to id, "crn" to crn)
    val createdByUser = userService.findUserByUserId(dtr.createdByUserId!!)
      .orThrowNotFound("id" to dtr.createdByUserId!!)
    val localAuthorityArea = localAuthorityAreaRepository.findByIdOrNull(command.localAuthorityAreaId)
      .orThrowNotFound("id" to command.localAuthorityAreaId)

    val aggregate = DutyToReferMapper.toAggregate(dtr).also {
      it.updateDutyToRefer(
        localAuthorityAreaId = command.localAuthorityAreaId,
        submissionDate = command.submissionDate,
        referenceNumber = command.referenceNumber,
        status = command.status,
      )
    }
    val updatedRecord = dutyToReferRepository.save(merge(aggregate.snapshot(), dtr))
    pullEventAndPersistToOutbox(aggregate)

    return DutyToReferMapper.toDto(
      snapshot = aggregate.snapshot(),
      crn = crn,
      createdBy = createdByUser.name,
      createdAt = updatedRecord.createdAt!!,
      localAuthorityAreaName = localAuthorityArea.name,
    )
  }

  @Transactional
  fun createDutyToReferNote(crn: String, id: UUID, noteCommand: NoteCommand) {
    val entity = dutyToReferRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val aggregate = DutyToReferMapper.toAggregate(entity)
    aggregate.addNote(note = noteCommand.note)
    val merged = merge(aggregate.snapshot(), entity)
    dutyToReferRepository.save(merged)
  }

  private fun pullEventAndPersistToOutbox(aggregate: DutyToReferAggregate) = aggregate.pullDomainEvents().forEach { event ->
    outboxEventRepository.save(
      OutboxEventEntity(
        id = UUID.randomUUID(),
        aggregateId = event.aggregateId,
        aggregateType = "DutyToRefer",
        domainEventType = event.type.name,
        payload = jsonMapper.writeValueAsString(event),
        processedStatus = ProcessedStatus.PENDING,
        createdAt = Instant.now(),
        processedAt = null,
      ),
    )
  }
}
