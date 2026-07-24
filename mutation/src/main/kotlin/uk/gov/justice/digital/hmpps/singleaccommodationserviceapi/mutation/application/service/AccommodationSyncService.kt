package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetailDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditOverrideContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationStatusEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
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
import java.time.LocalDate
import java.util.UUID

@Service
class AccommodationSyncService(
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val accommodationTypeRepository: AccommodationTypeRepository,
  private val accommodationStatusRepository: AccommodationStatusRepository,
  private val caseRepository: CaseRepository,
  private val userService: UserService,
  private val corePersonRecordCachingService: CorePersonRecordCachingService,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @Transactional
  fun syncAccommodationFromCpr(
    crn: String,
    cprAccommodations: List<AccommodationDetailDto>,
  ) {
    val case = caseRepository.findByCrn(crn)
      .orThrowNotFound("crn" to crn)
    syncAccommodationRecordsWithCpr(case, cprAccommodations)
  }

  private fun syncAccommodationRecordsWithCpr(case: CaseEntity, cprAccommodations: List<AccommodationDetailDto>) {
    cprAccommodations
      .forEach { cprAccommodation ->
        val sasProposedAccommodationRecord = proposedAccommodationRepository.findByCprAddressId(
          cprAddressId = cprAccommodation.cprAddressId!!,
        )
        if (!sasAccommodationRecordExists(sasProposedAccommodationRecord) && isProposedAccommodation(accommodation = cprAccommodation)) {
          insertDeliusOriginProposedAccommodationRecord(
            case = case,
            cprProposedAccommodationRecord = cprAccommodation,
          )
        } else if (sasAccommodationRecordExists(sasProposedAccommodationRecord)) {
          updateAccommodationRecordWithCprAddressUpdate(
            sasAccommodationRecord = sasProposedAccommodationRecord!!,
            cprAccommodationRecord = cprAccommodation,
          )
        }
      }
  }

  private fun sasAccommodationRecordExists(sasProposedAccommodationRecord: ProposedAccommodationEntity?) = sasProposedAccommodationRecord != null

  private fun isProposedAccommodation(accommodation: AccommodationDetailDto): Boolean = AddressStatusCode.PR.name == accommodation.status?.code ||
    AddressStatusCode.PR1.name == accommodation.status?.code

  private fun insertDeliusOriginProposedAccommodationRecord(
    case: CaseEntity,
    cprProposedAccommodationRecord: AccommodationDetailDto,
  ) {
    val (isAccommodationTypeNullOrExists, accommodationTypeEntityToUpdate) = accommodationTypeIsNullOrExists(cprProposedAccommodationRecord)
    val (isAccommodationStatusNullOrExists, accommodationStatusEntityToUpdate) = accommodationStatusIsNullOrExists(cprProposedAccommodationRecord)
    if (isAccommodationTypeNullOrExists && isAccommodationStatusNullOrExists) {
      val aggregate = ProposedAccommodationAggregate.hydrateNew(
        caseId = case.id,
        cprAddressId = cprProposedAccommodationRecord.cprAddressId,
        accommodationSource = AccommodationSource.DELIUS,
        currentAccommodation = null,
      )
      aggregate.syncProposedAccommodation(
        newAccommodationType = accommodationTypeEntityToUpdate?.let {
          AccommodationTypeDto(
            code = it.code,
            description = it.name,
          )
        },
        newAccommodationStatus = accommodationStatusEntityToUpdate?.let {
          AccommodationStatusDto(
            code = it.code,
            description = it.name,
          )
        },
        newAddress = cprProposedAccommodationRecord.address,
        newStartDate = cprProposedAccommodationRecord.startDate,
        newEndDate = cprProposedAccommodationRecord.endDate,
        newTypeVerified = cprProposedAccommodationRecord.typeVerified,
        newNoFixedAbode = cprProposedAccommodationRecord.noFixedAbode,
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

  fun updateAccommodationRecordWithCprAddressUpdate(
    crn: String,
    sasAccommodationRecord: ProposedAccommodationEntity,
    cprAddressRecord: CanonicalAddress,
  ): Boolean = updateAccommodationRecordWithCprAddressUpdate(
    sasAccommodationRecord = sasAccommodationRecord,
    cprAccommodationRecord = toAccommodationDetail(crn, cprAddressRecord),
  )

  private fun toAccommodationDetail(
    crn: String,
    address: CanonicalAddress,
  ) = AccommodationDetailDto(
    crn = crn,
    cprAddressId = UUID.fromString(address.cprAddressId),
    typeVerified = address.typeVerified,
    noFixedAbode = address.noFixedAbode,
    startDate = address.startDate?.let { LocalDate.parse(it) },
    endDate = address.endDate?.let { LocalDate.parse(it) },
    address = AccommodationAddressDetails(
      postcode = address.postcode,
      subBuildingName = address.subBuildingName,
      buildingName = address.buildingName,
      buildingNumber = address.buildingNumber,
      thoroughfareName = address.thoroughfareName,
      dependentLocality = address.dependentLocality,
      postTown = address.postTown,
      county = address.county,
      country = null,
      uprn = address.uprn,
    ),
    status = address.status.code?.let {
      AccommodationStatusDto(
        code = it,
        description = address.status.description,
      )
    },
    type = address.usages
      .firstOrNull { it.isActive && it.usageCode.code != null }
      ?.let {
        AccommodationTypeDto(
          code = it.usageCode.code!!,
          description = it.usageCode.description,
        )
      },
  )

  fun updateAccommodationRecordWithCprAddressUpdate(
    sasAccommodationRecord: ProposedAccommodationEntity,
    cprAccommodationRecord: AccommodationDetailDto,
  ): Boolean {
    val currentAccommodationTypeEntity = sasAccommodationRecord.accommodationTypeId?.let {
      accommodationTypeRepository.findByIdOrNull(it).orThrowNotFound("accommodationTypeId" to it)
    }
    val (isAccommodationTypeNullOrExists, accommodationTypeToUpdateEntity) = accommodationTypeIsNullOrExists(cprAccommodationRecord)
    val (isAccommodationStatusNullOrExists, accommodationStatusEntityToUpdate) = accommodationStatusIsNullOrExists(cprAccommodationRecord)
    if (isAccommodationTypeNullOrExists && isAccommodationStatusNullOrExists) {
      val aggregate = ProposedAccommodationMapper.toAggregate(
        sasAccommodationRecord,
        accommodationTypeEntity = currentAccommodationTypeEntity,
        accommodationStatusEntity = sasAccommodationRecord.accommodationStatusId?.let { accommodationStatusRepository.findByIdOrNull(it) },
        currentAccommodation = null,
      )
      aggregate.syncProposedAccommodation(
        newAccommodationType = accommodationTypeToUpdateEntity?.let {
          AccommodationTypeDto(
            code = it.code,
            description = it.name,
          )
        },
        newAccommodationStatus = accommodationStatusEntityToUpdate?.let {
          AccommodationStatusDto(
            code = it.code,
            description = it.name,
          )
        },
        newAddress = cprAccommodationRecord.address,
        newStartDate = cprAccommodationRecord.startDate,
        newEndDate = cprAccommodationRecord.endDate,
        newTypeVerified = cprAccommodationRecord.typeVerified,
        newNoFixedAbode = cprAccommodationRecord.noFixedAbode,
        syncType = SyncType.UPDATE,
      )
      val mergedRecord = merge(
        snapshot = aggregate.snapshot(),
        proposedAccommodationEntity = sasAccommodationRecord,
        accommodationTypeEntity = accommodationTypeToUpdateEntity,
        accommodationStatusEntity = accommodationStatusEntityToUpdate,
      )
      saveProposedAccommodation(proposedAccommodationEntity = mergedRecord)
      return true
    }
    return false
  }

  private fun accommodationTypeIsNullOrExists(
    cprProposedAccommodationRecord: AccommodationDetailDto,
  ): Pair<Boolean, AccommodationTypeEntity?> {
    if (cprProposedAccommodationRecord.type == null) {
      return true to null
    }
    val accommodationTypeEntity = accommodationTypeRepository.findByCode(cprProposedAccommodationRecord.type!!.code)
    if (accommodationTypeEntity == null) {
      log.error(
        """
          "A Delius origin proposed accommodation record with CPR address ID ${cprProposedAccommodationRecord.cprAddressId} has accommodation type code of ${cprProposedAccommodationRecord.type!!.code}.
          This accommodation type code is not a "Probation" Accommodation type and so this record cannot be mapped and synced to our proposed_accommodation table
        """.trimIndent(),
      )
      return false to null
    } else {
      return true to accommodationTypeEntity
    }
  }

  private fun accommodationStatusIsNullOrExists(
    cprProposedAccommodationRecord: AccommodationDetailDto,
  ): Pair<Boolean, AccommodationStatusEntity?> {
    if (cprProposedAccommodationRecord.status != null) {
      val accommodationStatusEntity = accommodationStatusRepository.findByCode(cprProposedAccommodationRecord.status!!.code)
      if (accommodationStatusEntity != null) {
        return true to accommodationStatusEntity
      } else {
        log.error(
          """
            "A Delius origin proposed accommodation record with CPR address ID ${cprProposedAccommodationRecord.cprAddressId} has accommodation status code of ${cprProposedAccommodationRecord.status!!.code}.
            This accommodation status code does not exist in our accommodation_status table so this record cannot be mapped and synced to our proposed_accommodation table
          """.trimIndent(),
        )
        return false to null
      }
    }
    return true to null
  }

  @Transactional
  fun softDeleteAccommodationRecordNoLongerInCpr(
    accommodationToDelete: ProposedAccommodationEntity,
  ) {
    accommodationToDelete.deleted = true
    proposedAccommodationRepository.save(accommodationToDelete)
    cprCacheEvict(caseId = accommodationToDelete.caseId)
  }

  private fun cprCacheEvict(caseId: UUID) {
    caseRepository.findByIdOrNull(caseId)?.let {
      corePersonRecordCachingService.cacheEvictOnCorePersonRecordByCrn(
        crn = it.latestCrn(),
      )
    }
  }
}
