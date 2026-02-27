package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CreateDtrCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.DutyToReferMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.DutyToReferAggregate
import java.time.Instant
import java.util.UUID

@Service
class DutyToReferApplicationService(
  private val jsonMapper: JsonMapper,
  private val dutyToReferRepository: DutyToReferRepository,
  private val outboxEventRepository: OutboxEventRepository,
  private val userService: UserService,
) {
  @Transactional
  fun createDutyToRefer(crn: String, command: CreateDtrCommand): DutyToReferDto {
    val user = userService.getUserForRequest()
    val aggregate = DutyToReferAggregate.hydrateNew(crn)
    aggregate.createDutyToRefer(
      localAuthorityAreaId = command.localAuthorityAreaId,
      submissionDate = command.submissionDate,
      referenceNumber = command.referenceNumber,
    )
    val persistedRecord = dutyToReferRepository.save(
      DutyToReferMapper.toEntity(aggregate.snapshot()),
    )
    pullEventAndPersistToOutbox(aggregate)
    return DutyToReferMapper.toDto(
      snapshot = aggregate.snapshot(),
      createdBy = user.name,
      createdAt = persistedRecord.createdAt!!,
    )
  }

  private fun pullEventAndPersistToOutbox(aggregate: DutyToReferAggregate) = aggregate.pullDomainEvents().forEach { event ->
    outboxEventRepository.save(
      OutboxEventEntity(
        id = UUID.randomUUID(),
        aggregateId = event.aggregateId,
        aggregateType = "DutyToRefer",
        domainEventType = event.type.name,
        payload = jsonMapper.writeValueAsString(event),
        createdAt = Instant.now(),
        processedStatus = ProcessedStatus.PENDING,
        processedAt = null,
      ),
    )
  }
}
