package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NoteCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OtherAccommodationReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OtherAccommodationReferralRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.OtherAccommodationReferralMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.OtherAccommodationReferralMapper.merge
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.OtherAccommodationReferralAggregate
import java.util.UUID

@Service
class OtherAccommodationReferralApplicationService(
  private val otherAccommodationReferralRepository: OtherAccommodationReferralRepository,
  private val userService: UserService,
  private val caseRepository: CaseRepository,
) {
  @Transactional
  fun createOtherAccommodationReferral(crn: String, command: OorCommand): OtherAccommodationReferralDto {
    val user = userService.authorizeAndRetrieveUser()
    val case = caseRepository.findByCrn(crn).orThrowNotFound("crn" to crn)
    val aggregate = OtherAccommodationReferralAggregate.hydrateNew(caseId = case.id)
    aggregate.updateOtherAccommodationReferral(
      submissionDate = command.submissionDate,
      referenceNumber = command.referenceNumber,
      status = command.status,
      submissionNote = command.submissionNote,
    )
    val persistedRecord = otherAccommodationReferralRepository.save(
      OtherAccommodationReferralMapper.toEntity(aggregate.snapshot()),
    )
    return OtherAccommodationReferralMapper.toDto(
      snapshot = aggregate.snapshot(),
      crn = crn,
      createdBy = user.displayName(),
      createdAt = persistedRecord.createdAt!!,
    )
  }

  @Transactional
  fun updateOtherAccommodationReferral(crn: String, id: UUID, command: OorCommand): OtherAccommodationReferralDto {
    val oor = otherAccommodationReferralRepository.findByIdAndCrn(id, crn)
      .orThrowNotFound("id" to id, "crn" to crn)
    val createdByUser = userService.findUserByUserId(oor.createdByUserId!!)
      .orThrowNotFound("id" to oor.createdByUserId!!)

    val aggregate = OtherAccommodationReferralMapper.toAggregate(oor).also {
      it.updateOtherAccommodationReferral(
        submissionDate = command.submissionDate,
        referenceNumber = command.referenceNumber,
        status = command.status,
        outcomeReason = command.outcomeReason,
        outcomeNote = command.outcomeNote,
        submissionNote = command.submissionNote,
      )
    }
    val updatedRecord = otherAccommodationReferralRepository.save(merge(aggregate.snapshot(), oor))

    return OtherAccommodationReferralMapper.toDto(
      snapshot = aggregate.snapshot(),
      crn = crn,
      createdBy = createdByUser.displayName(),
      createdAt = updatedRecord.createdAt!!,
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
