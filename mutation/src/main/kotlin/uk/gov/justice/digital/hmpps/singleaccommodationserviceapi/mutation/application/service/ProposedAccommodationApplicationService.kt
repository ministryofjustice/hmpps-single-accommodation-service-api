package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetailCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
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
  private val outboxEventRepository: OutboxEventRepository,
  private val userService: UserService,
  private val caseRepository: CaseRepository,
) {
  @Transactional
  fun createProposedAccommodation(crn: String, accommodationDetailCommand: AccommodationDetailCommand): AccommodationDetail {
    val user = userService.authorizeAndRetrieveUser()
    val case = caseRepository.findByCrn(crn).orThrowNotFound("crn" to crn)
    val aggregate = ProposedAccommodationAggregate.hydrateNew(case.id)
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
      crn = crn,
      createdBy = user.name,
      createdAt = persistedRecord.createdAt!!,
    )
  }

  @Transactional
  fun updateProposedAccommodation(crn: String, id: UUID, accommodationDetailCommand: AccommodationDetailCommand): AccommodationDetail {
    val entity = proposedAccommodationRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)
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

    val updatedRecord = proposedAccommodationRepository.save(merge(aggregate.snapshot(), entity))
    pullEventAndPersistToOutbox(aggregate)

    val createdByUser = userService.findUserByUserId(updatedRecord.createdByUserId!!)
      .orThrowNotFound("createdByUserId" to updatedRecord.createdByUserId!!)
    return ProposedAccommodationMapper.toDto(
      snapshot = aggregate.snapshot(),
      crn = crn,
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
