package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetailDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NoteCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ProposedAccommodationDetailCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ProposedAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditOverrideContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.ProbationCreateAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationStatusEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.OutboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationStatusRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.ProposedAccommodationMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.ProposedAccommodationMapper.merge
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
class ProposedAccommodationApplicationService(
  private val jsonMapper: JsonMapper,
  private val clock: Clock,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val accommodationTypeRepository: AccommodationTypeRepository,
  private val accommodationStatusRepository: AccommodationStatusRepository,
  private val outboxEventRepository: OutboxEventRepository,
  private val userService: UserService,
  private val caseRepository: CaseRepository,
  private val corePersonRecordClient: CorePersonRecordClient,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

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
    val (aggregate, persistedRecord) = createProposedAccommodationAndPersistToDatabase(
      caseId = case.id,
      cprAddressId = null,
      accommodationSource = AccommodationSource.SAS,
      currentAccommodation = currentAccommodation,
      proposedAccommodationDetailCommand = proposedAccommodationDetailCommand,
      accommodationTypeEntity = accommodationTypeEntity,
    )
    createProposedAccommodationInCprWhereApplicable(crn, aggregate, updateRecord = persistedRecord)
    return ProposedAccommodationMapper.toDto(
      snapshot = aggregate.snapshot(),
      crn = crn,
      createdBy = user.displayName(),
      createdAt = persistedRecord.createdAt!!,
    )
  }

  private fun createProposedAccommodationAndPersistToDatabase(
    caseId: UUID,
    cprAddressId: UUID?,
    accommodationSource: AccommodationSource,
    currentAccommodation: AccommodationSummaryDto?,
    proposedAccommodationDetailCommand: ProposedAccommodationDetailCommand,
    accommodationTypeEntity: AccommodationTypeEntity,
  ): Pair<ProposedAccommodationAggregate, ProposedAccommodationEntity> {
    val aggregate = ProposedAccommodationAggregate.hydrateNew(
      caseId = caseId,
      cprAddressId = cprAddressId,
      currentAccommodation = currentAccommodation,
    )
    aggregate.updateProposedAccommodation(
      newAccommodationSource = accommodationSource,
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
      newTypeVerified = null,
      newNoFixedAbode = false,
    )
    val accommodationStatusEntity = aggregate.snapshot().accommodationStatus
      ?.let {
        accommodationStatusRepository.findByCodeAndActiveIsTrue(it.code)
          .orThrowNotFound("code" to it.code)
      }
    val proposedAccommodationEntityToCreate = ProposedAccommodationMapper.toEntity(
      aggregate.snapshot(),
      accommodationTypeEntity,
      accommodationStatusEntity,
    )
    val persistedRecord = saveProposedAccommodation(proposedAccommodationEntityToCreate)
    return aggregate to persistedRecord
  }

  private fun saveProposedAccommodation(
    proposedAccommodationEntity: ProposedAccommodationEntity,
  ) = if (AccommodationSource.SAS == proposedAccommodationEntity.accommodationSource) {
    proposedAccommodationRepository.save(proposedAccommodationEntity)
  } else {
    val deliusSystemUser = userService.getNationalDeliusSystemUser()
    AuditOverrideContext.withAuditorId(deliusSystemUser.id) {
      proposedAccommodationRepository.save(proposedAccommodationEntity)
    }
  }

  fun createProposedAccommodationInCprWhereApplicable(
    crn: String,
    aggregate: ProposedAccommodationAggregate,
    updateRecord: ProposedAccommodationEntity,
  ) {
    if (aggregate.requiresCprRegistration()) {
      val aggregateSnapshot = aggregate.snapshot()
      val createdAccommodation = corePersonRecordClient.createProbationAddress(
        crn = crn,
        address = ProbationCreateAddress(
          noFixedAbode = false,
          startDate = Instant.now(clock),
          endDate = null,
          postcode = aggregateSnapshot.address.postcode,
          subBuildingName = aggregateSnapshot.address.subBuildingName,
          buildingName = aggregateSnapshot.address.buildingName,
          buildingNumber = aggregateSnapshot.address.buildingNumber,
          thoroughfareName = aggregateSnapshot.address.thoroughfareName,
          dependentLocality = aggregateSnapshot.address.dependentLocality,
          postTown = aggregateSnapshot.address.postTown,
          county = aggregateSnapshot.address.county,
          countryCode = null, // todo: might need to send this as an extra if the FE can get it from the lookup integration for us
          uprn = aggregateSnapshot.address.uprn,
          comment = null,
          statusCode = AddressStatusCode.valueOf(aggregateSnapshot.accommodationStatus!!.code),
          usages = listOf(
            AddressUsage(
              usageCode = AddressUsageCode.valueOf(aggregateSnapshot.accommodationType.code),
              isActive = true,
            ),
          ),
          contacts = emptyList(),
        ),
      )
      aggregate.markRegisteredWithCpr(createdAccommodation.cprAddressId)
      updateRecord.cprAddressId = aggregate.snapshot().cprAddressId
      proposedAccommodationRepository.save(updateRecord)
    }
  }

  @Transactional
  fun updateProposedAccommodation(
    id: UUID,
    crn: String,
    proposedAccommodationDetailCommand: ProposedAccommodationDetailCommand,
    currentAccommodation: AccommodationSummaryDto?,
  ): ProposedAccommodationDto {
    val proposedAccommodationEntity = proposedAccommodationRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val currentAccommodationTypeEntity = accommodationTypeRepository.findByIdOrNull(proposedAccommodationEntity.accommodationTypeId)!!
    val currentAccommodationStatusEntity = proposedAccommodationEntity.accommodationStatusId
      ?.let {
        accommodationStatusRepository.findByIdOrNull(it)
          .orThrowNotFound("id" to it)
      }
    val accommodationTypeEntityToUpdate = accommodationTypeRepository.findByCodeAndActiveIsTrue(proposedAccommodationDetailCommand.accommodationTypeCode)
      .orThrowNotFound("code" to proposedAccommodationDetailCommand.accommodationTypeCode)
    val (aggregate, updatedRecord) = updateProposedAccommodationAndPersistToDatabase(
      accommodationSource = AccommodationSource.SAS,
      proposedAccommodationEntity = proposedAccommodationEntity,
      currentAccommodationTypeEntity = currentAccommodationTypeEntity,
      accommodationTypeEntityToUpdate = accommodationTypeEntityToUpdate,
      currentAccommodationStatusEntity = currentAccommodationStatusEntity,
      proposedAccommodationDetailCommand,
      currentAccommodation,
    )
    createProposedAccommodationInCprWhereApplicable(crn, aggregate, updateRecord = updatedRecord)
    pullEventAndPersistToOutbox(aggregate)
    val createdByUser = userService.findUserByUserId(updatedRecord.createdByUserId!!)
      .orThrowNotFound("id" to updatedRecord.createdByUserId!!)
    return ProposedAccommodationMapper.toDto(
      snapshot = aggregate.snapshot(),
      crn = crn,
      createdBy = createdByUser.displayName(),
      createdAt = updatedRecord.createdAt!!,
    )
  }

  private fun updateProposedAccommodationAndPersistToDatabase(
    accommodationSource: AccommodationSource,
    proposedAccommodationEntity: ProposedAccommodationEntity,
    currentAccommodationTypeEntity: AccommodationTypeEntity,
    accommodationTypeEntityToUpdate: AccommodationTypeEntity,
    currentAccommodationStatusEntity: AccommodationStatusEntity?,
    proposedAccommodationDetailCommand: ProposedAccommodationDetailCommand,
    currentAccommodation: AccommodationSummaryDto?,
  ): Pair<ProposedAccommodationAggregate, ProposedAccommodationEntity> {
    val aggregate = ProposedAccommodationMapper.toAggregate(
      proposedAccommodationEntity,
      accommodationTypeEntity = currentAccommodationTypeEntity,
      accommodationStatusEntity = currentAccommodationStatusEntity,
      currentAccommodation,
    )
    aggregate.updateProposedAccommodation(
      newAccommodationSource = accommodationSource,
      newName = proposedAccommodationDetailCommand.name,
      newAccommodationType = AccommodationTypeDto(
        code = accommodationTypeEntityToUpdate.code,
        description = accommodationTypeEntityToUpdate.name,
      ),
      newVerificationStatus = proposedAccommodationDetailCommand.verificationStatus,
      newNextAccommodationStatus = proposedAccommodationDetailCommand.nextAccommodationStatus,
      newAddress = proposedAccommodationDetailCommand.address,
      newStartDate = proposedAccommodationDetailCommand.startDate,
      newEndDate = proposedAccommodationDetailCommand.endDate,
      newTypeVerified = null,
      newNoFixedAbode = false,
    )
    val mergedRecord = merge(
      snapshot = aggregate.snapshot(),
      proposedAccommodationEntity,
      accommodationTypeEntityToUpdate,
      accommodationStatusEntity = getAccommodationStatusEntity(
        preUpdateAccommodationStatusEntity = currentAccommodationStatusEntity,
        aggregate,
      ),
    )
    val updatedRecord = saveProposedAccommodation(proposedAccommodationEntity = mergedRecord)
    return aggregate to updatedRecord
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
        createdAt = Instant.now(clock),
        processedStatus = ProcessedStatus.PENDING,
        processedAt = null,
      ),
    )
  }

  @Transactional
  fun syncProposedAccommodationFromDelius(
    crn: String,
    currentAccommodation: AccommodationSummaryDto?,
    cprAccommodations: List<AccommodationDetailDto>,
  ) {
    val case = caseRepository.findByCrn(crn)
      .orThrowNotFound("crn" to crn)
    cprAccommodations
      .filter { isProposedAccommodation(accommodation = it) }
      .forEach { cprProposedAccommodation ->
        val sasProposedAccommodationRecord = proposedAccommodationRepository.findByCprAddressId(
          cprAddressId = cprProposedAccommodation.cprAddressId,
        )
        if (sasProposedAccommodationRecord == null) {
          insertDeliusOriginProposedAccommodationRecord(
            case = case,
            deliusProposedAccommodationRecord = cprProposedAccommodation,
            currentAccommodation = currentAccommodation,
          )
        } else {
          updateDeliusOriginProposedAccommodationRecord(
            sasProposedAccommodationRecord = sasProposedAccommodationRecord,
            deliusProposedAccommodationRecord = cprProposedAccommodation,
            currentAccommodation = currentAccommodation,
          )
        }
      }
  }

  private fun isProposedAccommodation(accommodation: AccommodationDetailDto): Boolean = AddressStatusCode.PR.name == accommodation.status?.code ||
    AddressStatusCode.PR1.name == accommodation.status?.code

  private fun insertDeliusOriginProposedAccommodationRecord(
    case: CaseEntity,
    deliusProposedAccommodationRecord: AccommodationDetailDto,
    currentAccommodation: AccommodationSummaryDto?,
  ) {
    val (isAccommodationTypeNullOrNonProbation, accommodationTypeEntity) = isAccommodationTypeNullOrNonProbation(deliusProposedAccommodationRecord)
    if (!isAccommodationTypeNullOrNonProbation) {
      createProposedAccommodationAndPersistToDatabase(
        caseId = case.id,
        cprAddressId = deliusProposedAccommodationRecord.cprAddressId,
        accommodationSource = AccommodationSource.DELIUS,
        currentAccommodation = currentAccommodation,
        proposedAccommodationDetailCommand = ProposedAccommodationDetailCommand(
          name = null,
          accommodationTypeCode = accommodationTypeEntity!!.code,
          verificationStatus = VerificationStatus.PASSED,
          nextAccommodationStatus = NextAccommodationStatus.YES,
          address = deliusProposedAccommodationRecord.address,
          startDate = deliusProposedAccommodationRecord.startDate,
          endDate = deliusProposedAccommodationRecord.endDate,
        ),
        accommodationTypeEntity = accommodationTypeEntity,
      )
    }
  }

  private fun updateDeliusOriginProposedAccommodationRecord(
    sasProposedAccommodationRecord: ProposedAccommodationEntity,
    deliusProposedAccommodationRecord: AccommodationDetailDto,
    currentAccommodation: AccommodationSummaryDto?,
  ) {
    val currentAccommodationTypeEntity = accommodationTypeRepository.findByIdOrNull(sasProposedAccommodationRecord.accommodationTypeId)!!
    val (isAccommodationTypeNullOrNonProbation, accommodationTypeEntity) = isAccommodationTypeNullOrNonProbation(deliusProposedAccommodationRecord)
    if (!isAccommodationTypeNullOrNonProbation) {
      updateProposedAccommodationAndPersistToDatabase(
        accommodationSource = sasProposedAccommodationRecord.accommodationSource,
        proposedAccommodationEntity = sasProposedAccommodationRecord,
        currentAccommodationTypeEntity = currentAccommodationTypeEntity,
        accommodationTypeEntityToUpdate = accommodationTypeEntity!!,
        currentAccommodationStatusEntity = sasProposedAccommodationRecord.accommodationStatusId?.let { accommodationStatusRepository.findByIdOrNull(it) },
        proposedAccommodationDetailCommand = ProposedAccommodationDetailCommand(
          name = null,
          accommodationTypeCode = accommodationTypeEntity.code,
          verificationStatus = VerificationStatus.valueOf(sasProposedAccommodationRecord.verificationStatus!!.name),
          nextAccommodationStatus = NextAccommodationStatus.valueOf(sasProposedAccommodationRecord.nextAccommodationStatus!!.name),
          address = deliusProposedAccommodationRecord.address,
          startDate = deliusProposedAccommodationRecord.startDate,
          endDate = deliusProposedAccommodationRecord.endDate,
        ),
        currentAccommodation = currentAccommodation,
      )
    }
  }

  private fun isAccommodationTypeNullOrNonProbation(
    deliusProposedAccommodationRecord: AccommodationDetailDto,
  ): Pair<Boolean, AccommodationTypeEntity?> {
    if (deliusProposedAccommodationRecord.type == null) {
      log.error(
        """
          "A Delius origin proposed accommodation record with CPR address ID ${deliusProposedAccommodationRecord.cprAddressId} does not have an accommodation type code.
           This accommodation type code is mandatory for SAS Proposed Accommodation and so this record cannot be mapped and synced to our proposed_accommodation table
        """.trimIndent(),
      )
      return true to null
    } else {
      val accommodationTypeEntity =
        accommodationTypeRepository.findByCode(deliusProposedAccommodationRecord.type!!.code)
      if (accommodationTypeEntity == null) {
        log.error(
          """
            "A Delius origin proposed accommodation record with CPR address ID ${deliusProposedAccommodationRecord.cprAddressId} has accommodation type code of ${deliusProposedAccommodationRecord.type!!.code}.
            This accommodation type code is not a "Probation" Accommodation type and so this record cannot be mapped and synced to our proposed_accommodation table
          """.trimIndent(),
        )
        return true to null
      } else {
        return false to accommodationTypeEntity
      }
    }
  }
}
