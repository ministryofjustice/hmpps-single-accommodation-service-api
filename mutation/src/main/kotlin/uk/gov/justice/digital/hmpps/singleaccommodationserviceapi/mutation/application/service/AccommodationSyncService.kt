package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetailDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditOverrideContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationStatusEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationStatusRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.ProposedAccommodationMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.ProposedAccommodationMapper.merge
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.SyncType

@Service
class AccommodationSyncService(
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val accommodationTypeRepository: AccommodationTypeRepository,
  private val accommodationStatusRepository: AccommodationStatusRepository,
  private val caseRepository: CaseRepository,
  private val userService: UserService,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @Transactional
  fun syncAccommodationFromDelius(
    crn: String,
    cprAccommodations: List<AccommodationDetailDto>,
  ) {
    val case = caseRepository.findByCrn(crn)
      .orThrowNotFound("crn" to crn)
    syncAccommodationRecordsWithCpr(case, cprAccommodations)
    deleteAccommodationRecordsNoLongerInCpr(case, cprAccommodations)
  }

  private fun syncAccommodationRecordsWithCpr(case: CaseEntity, cprAccommodations: List<AccommodationDetailDto>) {
    cprAccommodations
      .forEach { cprAccommodation ->
        val sasProposedAccommodationRecord = proposedAccommodationRepository.findByCprAddressId(
          cprAddressId = cprAccommodation.cprAddressId,
        )
        if (!sasAccommodationRecordExists(sasProposedAccommodationRecord) && isProposedAccommodation(accommodation = cprAccommodation)) {
          insertDeliusOriginProposedAccommodationRecord(
            case = case,
            deliusProposedAccommodationRecord = cprAccommodation,
          )
        } else if (sasAccommodationRecordExists(sasProposedAccommodationRecord)) {
          updateAccommodationRecordWithDeliusUpdate(
            sasProposedAccommodationRecord = sasProposedAccommodationRecord!!,
            deliusProposedAccommodationRecord = cprAccommodation,
          )
        }
      }
  }

  private fun sasAccommodationRecordExists(sasProposedAccommodationRecord: ProposedAccommodationEntity?) = sasProposedAccommodationRecord != null

  private fun isProposedAccommodation(accommodation: AccommodationDetailDto): Boolean = AddressStatusCode.PR.name == accommodation.status?.code ||
    AddressStatusCode.PR1.name == accommodation.status?.code

  private fun insertDeliusOriginProposedAccommodationRecord(
    case: CaseEntity,
    deliusProposedAccommodationRecord: AccommodationDetailDto,
  ) {
    val (isAccommodationTypeNullOrNonProbation, accommodationTypeEntityToUpdate) = accommodationTypeIsNullOrNonProbation(deliusProposedAccommodationRecord)
    val (isAccommodationStatusNullOrExists, accommodationStatusEntityToUpdate) = accommodationStatusIsNullOrExists(deliusProposedAccommodationRecord)
    if (!isAccommodationTypeNullOrNonProbation && isAccommodationStatusNullOrExists) {
      val aggregate = ProposedAccommodationAggregate.hydrateNew(
        caseId = case.id,
        cprAddressId = deliusProposedAccommodationRecord.cprAddressId,
        accommodationSource = AccommodationSource.DELIUS,
        currentAccommodation = null,
      )
      aggregate.syncProposedAccommodation(
        newAccommodationType = AccommodationTypeDto(
          code = accommodationTypeEntityToUpdate!!.code,
          description = accommodationTypeEntityToUpdate.name,
        ),
        newAccommodationStatus = accommodationStatusEntityToUpdate?.let {
          AccommodationStatusDto(
            code = it.code,
            description = it.name,
          )
        },
        newAddress = deliusProposedAccommodationRecord.address,
        newStartDate = deliusProposedAccommodationRecord.startDate,
        newEndDate = deliusProposedAccommodationRecord.endDate,
        newTypeVerified = deliusProposedAccommodationRecord.typeVerified,
        newNoFixedAbode = deliusProposedAccommodationRecord.noFixedAbode,
        syncType = SyncType.CREATE,
      )
      val proposedAccommodationEntityToCreate = ProposedAccommodationMapper.toEntity(
        aggregate.snapshot(),
        accommodationTypeEntityToUpdate,
        accommodationStatusEntityToUpdate,
      )
      saveProposedAccommodation(proposedAccommodationEntityToCreate)
    }
  }

  private fun saveProposedAccommodation(
    proposedAccommodationEntity: ProposedAccommodationEntity,
  ): ProposedAccommodationEntity {
    val deliusSystemUser = userService.getNationalDeliusSystemUser()
    return AuditOverrideContext.withAuditorId(deliusSystemUser.id) {
      proposedAccommodationRepository.save(proposedAccommodationEntity)
    }
  }

  private fun updateAccommodationRecordWithDeliusUpdate(
    sasProposedAccommodationRecord: ProposedAccommodationEntity,
    deliusProposedAccommodationRecord: AccommodationDetailDto,
  ) {
    val currentAccommodationTypeEntity = accommodationTypeRepository.findByIdOrNull(sasProposedAccommodationRecord.accommodationTypeId)!!
    val (isAccommodationTypeNullOrNonProbation, accommodationTypeToUpdateEntity) = accommodationTypeIsNullOrNonProbation(deliusProposedAccommodationRecord)
    val (isAccommodationStatusNullOrExists, accommodationStatusEntityToUpdate) = accommodationStatusIsNullOrExists(deliusProposedAccommodationRecord)
    if (!isAccommodationTypeNullOrNonProbation && isAccommodationStatusNullOrExists) {
      val aggregate = ProposedAccommodationMapper.toAggregate(
        sasProposedAccommodationRecord,
        accommodationTypeEntity = currentAccommodationTypeEntity,
        accommodationStatusEntity = sasProposedAccommodationRecord.accommodationStatusId?.let { accommodationStatusRepository.findByIdOrNull(it) },
        currentAccommodation = null,
      )
      aggregate.syncProposedAccommodation(
        newAccommodationType = AccommodationTypeDto(
          code = accommodationTypeToUpdateEntity!!.code,
          description = accommodationTypeToUpdateEntity.name,
        ),
        newAccommodationStatus = accommodationStatusEntityToUpdate?.let {
          AccommodationStatusDto(
            code = it.code,
            description = it.name,
          )
        },
        newAddress = deliusProposedAccommodationRecord.address,
        newStartDate = deliusProposedAccommodationRecord.startDate,
        newEndDate = deliusProposedAccommodationRecord.endDate,
        newTypeVerified = deliusProposedAccommodationRecord.typeVerified,
        newNoFixedAbode = deliusProposedAccommodationRecord.noFixedAbode,
        syncType = SyncType.UPDATE,
      )
      val mergedRecord = merge(
        snapshot = aggregate.snapshot(),
        proposedAccommodationEntity = sasProposedAccommodationRecord,
        accommodationTypeEntity = accommodationTypeToUpdateEntity,
        accommodationStatusEntity = accommodationStatusEntityToUpdate,
      )
      saveProposedAccommodation(proposedAccommodationEntity = mergedRecord)
    }
  }

  private fun accommodationTypeIsNullOrNonProbation(
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

  private fun accommodationStatusIsNullOrExists(
    deliusProposedAccommodationRecord: AccommodationDetailDto,
  ): Pair<Boolean, AccommodationStatusEntity?> {
    if (deliusProposedAccommodationRecord.status != null) {
      val accommodationStatusEntity = accommodationStatusRepository.findByCode(deliusProposedAccommodationRecord.status!!.code)
      if (accommodationStatusEntity != null) {
        return true to accommodationStatusEntity
      } else {
        log.error(
          """
            "A Delius origin proposed accommodation record with CPR address ID ${deliusProposedAccommodationRecord.cprAddressId} has accommodation status code of ${deliusProposedAccommodationRecord.status!!.code}.
            This accommodation status code does not exist in our accommodation_status table so this record cannot be mapped and synced to our proposed_accommodation table
          """.trimIndent(),
        )
        return false to null
      }
    }
    return true to null
  }

  private fun deleteAccommodationRecordsNoLongerInCpr(
    case: CaseEntity,
    cprAccommodations: List<AccommodationDetailDto>,
  ) {
    val cprAddressIds = cprAccommodations.mapNotNull { it.cprAddressId }.toSet()
    val accommodationsToDelete = proposedAccommodationRepository.findByCaseId(case.id)
      .filter { NextAccommodationStatus.YES == it.nextAccommodationStatus }
      .filter { accommodation ->
        accommodation.cprAddressId !in cprAddressIds
      }
    accommodationsToDelete.forEach { accommodation ->
      accommodation.deleted = true
    }
    val deliusSystemUser = userService.getNationalDeliusSystemUser()
    AuditOverrideContext.withAuditorId(deliusSystemUser.id) {
      proposedAccommodationRepository.saveAll(accommodationsToDelete)
    }
  }
}
