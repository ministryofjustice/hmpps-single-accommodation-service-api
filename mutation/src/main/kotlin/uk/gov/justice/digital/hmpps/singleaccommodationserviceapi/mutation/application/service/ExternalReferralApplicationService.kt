package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ExternalReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NoteCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ExternalReferralRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.ExternalReferralMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.ExternalReferralMapper.merge
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ExternalReferralAggregate
import java.util.UUID

@Service
class ExternalReferralApplicationService(
  private val externalReferralRepository: ExternalReferralRepository,
  private val caseRepository: CaseRepository,
  private val userService: UserService,
) {
  @Transactional
  fun createExternalReferral(crn: String, command: ExternalReferralCommand): ExternalReferralDto {
    val user = userService.authorizeAndRetrieveUser()
    val case = caseRepository.findByCrn(crn).orThrowNotFound("crn" to crn)

    val aggregate = ExternalReferralAggregate.hydrateNew(caseId = case.id, crn = crn)
    aggregate.updateExternalReferral(
      submissionDate = command.submissionDate,
      referenceNumber = command.referenceNumber,
      status = command.status,
      organisationName = command.organisationName,
      website = command.website,
      submissionNote = command.submissionNote,
      email = command.email,
      phoneNumber = command.phoneNumber,
    )

    val persistedRecord = externalReferralRepository.save(
      ExternalReferralMapper.toEntity(aggregate.snapshot()),
    )

    return ExternalReferralMapper.toDto(
      snapshot = aggregate.snapshot(),
      createdBy = user.displayName(),
      createdByUsername = user.username,
      createdAt = persistedRecord.createdAt,
    )
  }

  @Transactional
  fun updateExternalReferral(crn: String, id: UUID, command: ExternalReferralCommand): ExternalReferralDto {
    val referral = externalReferralRepository.findByIdAndCrn(id, crn)
      .orThrowNotFound("id" to id, "crn" to crn)
    val createdByUser = userService.findUserByUserId(referral.createdByUserId)
      .orThrowNotFound("id" to referral.createdByUserId)

    val aggregate = ExternalReferralMapper.toAggregate(referral).also {
      it.updateExternalReferral(
        submissionDate = command.submissionDate,
        referenceNumber = command.referenceNumber,
        status = command.status,
        organisationName = command.organisationName,
        website = command.website,
        submissionNote = command.submissionNote,
        email = command.email,
        phoneNumber = command.phoneNumber,
        withdrawalReason = command.withdrawalReason,
        withdrawalNote = command.withdrawalNote,
        outcomeNote = command.outcomeNote,
      )
    }
    val updatedRecord = externalReferralRepository.save(merge(aggregate.snapshot(), referral))

    return ExternalReferralMapper.toDto(
      snapshot = aggregate.snapshot(),
      createdBy = createdByUser.displayName(),
      createdByUsername = createdByUser.username,
      createdAt = updatedRecord.createdAt,
    )
  }

  @Transactional
  fun createExternalReferralNote(crn: String, id: UUID, noteCommand: NoteCommand) {
    val entity = externalReferralRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val aggregate = ExternalReferralMapper.toAggregate(entity)
    aggregate.addNote(note = noteCommand.note)
    val merged = merge(aggregate.snapshot(), entity)
    externalReferralRepository.save(merged)
  }
}
