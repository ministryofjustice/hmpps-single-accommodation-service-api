package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ProposedAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationStatusEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate.ProposedAccommodationSnapshot
import java.time.Instant
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.NextAccommodationStatus as EntityNextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.VerificationStatus as EntityVerificationStatus

object ProposedAccommodationMapper {

  fun toEntity(
    snapshot: ProposedAccommodationSnapshot,
    accommodationTypeEntity: AccommodationTypeEntity?,
    accommodationStatusEntity: AccommodationStatusEntity?,
  ) = ProposedAccommodationEntity(
    id = snapshot.id,
    caseId = snapshot.caseId,
    cprAddressId = snapshot.cprAddressId,
    accommodationSource = snapshot.accommodationSource,
    name = snapshot.name,
    accommodationTypeId = accommodationTypeEntity?.id,
    accommodationStatusId = accommodationStatusEntity?.id,
    verificationStatus = EntityVerificationStatus.valueOf(snapshot.verificationStatus.name),
    nextAccommodationStatus = EntityNextAccommodationStatus.valueOf(snapshot.nextAccommodationStatus.name),
    startDate = snapshot.startDate,
    endDate = snapshot.endDate,
    typeVerified = snapshot.typeVerified,
    noFixedAbode = snapshot.noFixedAbode,
    postcode = snapshot.address.postcode,
    subBuildingName = convertEmptyAddressFieldToToNull(snapshot.address.subBuildingName),
    buildingName = convertEmptyAddressFieldToToNull(snapshot.address.buildingName),
    buildingNumber = convertEmptyAddressFieldToToNull(snapshot.address.buildingNumber),
    thoroughfareName = convertEmptyAddressFieldToToNull(snapshot.address.thoroughfareName),
    dependentLocality = convertEmptyAddressFieldToToNull(snapshot.address.dependentLocality),
    postTown = convertEmptyAddressFieldToToNull(snapshot.address.postTown),
    county = convertEmptyAddressFieldToToNull(snapshot.address.county),
    country = null,
    uprn = convertEmptyAddressFieldToToNull(snapshot.address.uprn),
  )

  fun merge(
    snapshot: ProposedAccommodationSnapshot,
    proposedAccommodationEntity: ProposedAccommodationEntity,
    accommodationTypeEntity: AccommodationTypeEntity?,
    accommodationStatusEntity: AccommodationStatusEntity?,
  ): ProposedAccommodationEntity {
    proposedAccommodationEntity.cprAddressId = snapshot.cprAddressId
    proposedAccommodationEntity.accommodationSource = snapshot.accommodationSource
    proposedAccommodationEntity.name = snapshot.name
    proposedAccommodationEntity.accommodationTypeId = accommodationTypeEntity?.id
    proposedAccommodationEntity.accommodationStatusId = accommodationStatusEntity?.id
    proposedAccommodationEntity.verificationStatus = EntityVerificationStatus.valueOf(snapshot.verificationStatus.name)
    proposedAccommodationEntity.nextAccommodationStatus = EntityNextAccommodationStatus.valueOf(snapshot.nextAccommodationStatus.name)
    proposedAccommodationEntity.startDate = snapshot.startDate
    proposedAccommodationEntity.endDate = snapshot.endDate
    proposedAccommodationEntity.typeVerified = snapshot.typeVerified
    proposedAccommodationEntity.noFixedAbode = snapshot.noFixedAbode
    proposedAccommodationEntity.postcode = snapshot.address.postcode
    proposedAccommodationEntity.subBuildingName = convertEmptyAddressFieldToToNull(snapshot.address.subBuildingName)
    proposedAccommodationEntity.buildingName = convertEmptyAddressFieldToToNull(snapshot.address.buildingName)
    proposedAccommodationEntity.buildingNumber = convertEmptyAddressFieldToToNull(snapshot.address.buildingNumber)
    proposedAccommodationEntity.thoroughfareName = convertEmptyAddressFieldToToNull(snapshot.address.thoroughfareName)
    proposedAccommodationEntity.dependentLocality = convertEmptyAddressFieldToToNull(snapshot.address.dependentLocality)
    proposedAccommodationEntity.postTown = convertEmptyAddressFieldToToNull(snapshot.address.postTown)
    proposedAccommodationEntity.county = convertEmptyAddressFieldToToNull(snapshot.address.county)
    proposedAccommodationEntity.country = null
    proposedAccommodationEntity.uprn = convertEmptyAddressFieldToToNull(snapshot.address.uprn)
    proposedAccommodationEntity.addMissingNotes(snapshot.notes)
    return proposedAccommodationEntity
  }

  private fun convertEmptyAddressFieldToToNull(addressField: String?) = addressField?.let { addressField.ifEmpty { null } }

  fun ProposedAccommodationEntity.addMissingNotes(snapshotNotes: List<ProposedAccommodationAggregate.ProposedAccommodationNoteSnapshot>) {
    val existingIds = this.notes.map { it.id }.toSet()
    val missingNotes = snapshotNotes
      .filter { it.id !in existingIds }
      .map {
        ProposedAccommodationNoteEntity(
          id = it.id,
          note = it.note,
          proposedAccommodation = this,
        )
      }
    this.notes.addAll(missingNotes)
  }

  fun toAggregate(
    proposedAccommodationEntity: ProposedAccommodationEntity,
    accommodationTypeEntity: AccommodationTypeEntity?,
    accommodationStatusEntity: AccommodationStatusEntity?,
    currentAccommodation: AccommodationSummaryDto?,
  ): ProposedAccommodationAggregate = ProposedAccommodationAggregate.hydrateExisting(
    id = proposedAccommodationEntity.id,
    caseId = proposedAccommodationEntity.caseId,
    accommodationSource = proposedAccommodationEntity.accommodationSource,
    cprAddressId = proposedAccommodationEntity.cprAddressId,
    currentAccommodation = currentAccommodation,
    name = proposedAccommodationEntity.name,
    accommodationType = accommodationTypeEntity?.let {
      AccommodationTypeDto(
        code = it.code,
        description = it.name,
      )
    },
    accommodationStatus = accommodationStatusEntity?.let {
      AccommodationStatusDto(
        code = accommodationStatusEntity.code,
        description = accommodationStatusEntity.name,
      )
    },
    verificationStatus = VerificationStatus.valueOf(proposedAccommodationEntity.verificationStatus!!.name),
    nextAccommodationStatus = NextAccommodationStatus.valueOf(proposedAccommodationEntity.nextAccommodationStatus!!.name),
    address = AccommodationAddressDetails(
      postcode = proposedAccommodationEntity.postcode,
      subBuildingName = proposedAccommodationEntity.subBuildingName,
      buildingName = proposedAccommodationEntity.buildingName,
      buildingNumber = proposedAccommodationEntity.buildingNumber,
      thoroughfareName = proposedAccommodationEntity.thoroughfareName,
      dependentLocality = proposedAccommodationEntity.dependentLocality,
      postTown = proposedAccommodationEntity.postTown,
      county = proposedAccommodationEntity.county,
      country = proposedAccommodationEntity.country,
      uprn = proposedAccommodationEntity.uprn,
    ),
    startDate = proposedAccommodationEntity.startDate,
    endDate = proposedAccommodationEntity.endDate,
    typeVerified = proposedAccommodationEntity.typeVerified,
    noFixedAbode = proposedAccommodationEntity.noFixedAbode,
    notes = proposedAccommodationEntity.notes.map {
      ProposedAccommodationAggregate.ProposedAccommodationNoteSnapshot(
        id = it.id,
        note = it.note,
      )
    },
  )

  fun toDto(
    snapshot: ProposedAccommodationSnapshot,
    crn: String,
    createdBy: String,
    createdAt: Instant,
  ) = ProposedAccommodationDto(
    id = snapshot.id,
    crn = crn,
    name = snapshot.name,
    accommodationType = snapshot.accommodationType?.let {
      AccommodationTypeDto(
        code = it.code,
        description = it.description,
      )
    },
    verificationStatus = snapshot.verificationStatus,
    nextAccommodationStatus = snapshot.nextAccommodationStatus,
    startDate = snapshot.startDate,
    endDate = snapshot.endDate,
    address = snapshot.address,
    createdBy = createdBy,
    createdAt = createdAt,
  )
}
