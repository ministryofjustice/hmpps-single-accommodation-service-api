package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NoteCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OtherAccommodationReferralRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.OtherAccommodationReferralMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.OtherAccommodationReferralMapper.merge
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.OtherAccommodationReferralAggregate
import java.util.UUID

@Service
class OtherAccommodationReferralApplicationService(
  private val otherAccommodationReferralRepository: OtherAccommodationReferralRepository,
  private val localAuthorityAreaRepository: LocalAuthorityAreaRepository,
  private val caseRepository: CaseRepository,
  private val userService: UserService,
) {
  @Transactional
  fun createOtherAccommodationReferral(crn: String, command: OtherAccommodationReferralCommand): OtherAccommodationReferralDto {
    val user = userService.authorizeAndRetrieveUser()
    val case = caseRepository.findByCrn(crn).orThrowNotFound("crn" to crn)
    val localAuthorityArea = localAuthorityAreaRepository.findByIdOrNull(command.localAuthorityAreaId)
      .orThrowNotFound("id" to command.localAuthorityAreaId)

    val aggregate = OtherAccommodationReferralAggregate.hydrateNew(caseId = case.id, crn = crn)
    aggregate.updateOtherAccommodationReferral(
      localAuthorityAreaId = command.localAuthorityAreaId,
      submissionDate = command.submissionDate,
      referenceNumber = command.referenceNumber,
      status = command.status,
      organisationName = command.organisationName,
      website = command.website,
      submissionNote = command.submissionNote,
    )

    val persistedRecord = otherAccommodationReferralRepository.save(
      OtherAccommodationReferralMapper.toEntity(aggregate.snapshot()),
    )

    return OtherAccommodationReferralMapper.toDto(
      snapshot = aggregate.snapshot(),
      createdBy = user.displayName(),
      createdByUsername = user.username,
      createdAt = persistedRecord.createdAt!!,
      localAuthorityAreaName = localAuthorityArea.name,
    )
  }

  @Transactional
  fun updateOtherAccommodationReferral(crn: String, id: UUID, command: OtherAccommodationReferralCommand): OtherAccommodationReferralDto {
    val referral = otherAccommodationReferralRepository.findByIdAndCrn(id, crn)
      .orThrowNotFound("id" to id, "crn" to crn)
    val createdByUser = userService.findUserByUserId(referral.createdByUserId!!)
      .orThrowNotFound("id" to referral.createdByUserId!!)
    val localAuthorityArea = localAuthorityAreaRepository.findByIdOrNull(command.localAuthorityAreaId)
      .orThrowNotFound("id" to command.localAuthorityAreaId)

    val aggregate = OtherAccommodationReferralMapper.toAggregate(referral).also {
      it.updateOtherAccommodationReferral(
        localAuthorityAreaId = command.localAuthorityAreaId,
        submissionDate = command.submissionDate,
        referenceNumber = command.referenceNumber,
        status = command.status,
        organisationName = command.organisationName,
        website = command.website,
        submissionNote = command.submissionNote,
      )
    }
    val updatedRecord = otherAccommodationReferralRepository.save(merge(aggregate.snapshot(), referral))

    return OtherAccommodationReferralMapper.toDto(
      snapshot = aggregate.snapshot(),
      createdBy = createdByUser.displayName(),
      createdByUsername = createdByUser.username,
      createdAt = updatedRecord.createdAt!!,
      localAuthorityAreaName = localAuthorityArea.name,
    )
  }

  @Transactional
  fun createOtherAccommodationReferralNote(crn: String, id: UUID, noteCommand: NoteCommand) {
    val entity = otherAccommodationReferralRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val aggregate = OtherAccommodationReferralMapper.toAggregate(entity)
    aggregate.addNote(note = noteCommand.note)
    val merged = merge(aggregate.snapshot(), entity)
    otherAccommodationReferralRepository.save(merged)
  }
}
